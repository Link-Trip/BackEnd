package com.linktrip.output.http.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.HttpOptions
import com.google.genai.types.Part
import com.linktrip.application.domain.video.VideoAnalysisResult
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.http.dto.AiApiResponse
import com.linktrip.output.http.properties.GcpProperties
import io.github.thoroldvix.api.TranscriptApiFactory
import io.github.thoroldvix.api.YoutubeTranscriptApi
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.FileInputStream
import javax.annotation.PreDestroy

private val logger = KotlinLogging.logger {}

/**
 * YouTube 영상을 분석하여 여행 정보를 추출하는 어댑터.
 *
 * 자막 추출 → Gemini 텍스트 분석 (저렴) 을 우선 시도하고,
 * 자막이 없는 영상은 Gemini에 영상 URL을 직접 전달하여 분석한다 (fallback). -> 임시 미처리
 */
@Component
class VideoAnalyzeAdapter(
    private val gcpProperties: GcpProperties,
    private val objectMapper: ObjectMapper,
) : VideoAnalyzePort {
    private val credentials: GoogleCredentials by lazy {
        FileInputStream(gcpProperties.credentialsPath).use { stream ->
            GoogleCredentials.fromStream(stream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
        }
    }

    private val client: Client by lazy {
        Client
            .builder()
            .vertexAI(true)
            .project(gcpProperties.projectId)
            .location(gcpProperties.vertexAi.location)
            .httpOptions(HttpOptions.builder().apiVersion(API_VERSION).build())
            .credentials(credentials)
            .build()
    }

    private val transcriptApi: YoutubeTranscriptApi by lazy {
        TranscriptApiFactory.createDefault()
    }

    @PreDestroy
    fun close() {
        runCatching { client.close() }
            .onFailure { logger.warn(it) { "Gemini Client 종료 중 에러 발생" } }
    }

    override fun analyze(youtubeUrl: String): VideoAnalysisResult {
        val videoId = extractVideoId(youtubeUrl)
        val transcript = tryExtractTranscript(videoId)

        if (transcript == null) {
            logger.warn { "자막 없음, 분석 불가: videoId=$videoId" }
            throw LinktripException(ExceptionCode.BAD_REQUEST_VIDEO, "자막을 추출할 수 없는 영상입니다.")
        }

        logger.info { "자막 기반 분석 시작: videoId=$videoId (${transcript.length}자)" }
        logger.debug { "자막 원문 (마지막 500자):\n${transcript.takeLast(500)}" }
        return analyzeFromTranscript(transcript, videoId)
    }

    private fun tryExtractTranscript(videoId: String): String? =
        try {
            val transcriptList = transcriptApi.listTranscripts(videoId)
            val transcript =
                runCatching { transcriptList.findTranscript("ko") }
                    .recoverCatching { transcriptList.findTranscript("en") }
                    .recoverCatching { transcriptList.findGeneratedTranscript("ko") }
                    .recoverCatching { transcriptList.findGeneratedTranscript("en") }
                    .getOrNull()

            transcript?.fetch()?.let { content ->
                content.content.joinToString("\n") { fragment ->
                    "[${formatTimestamp(fragment.start.toLong())}] ${fragment.text}"
                }
            }
        } catch (e: Exception) {
            logger.warn { "자막 추출 실패 (videoId=$videoId): ${e.message}" }
            null
        }

    private fun analyzeFromTranscript(
        transcript: String,
        videoId: String,
    ): VideoAnalysisResult {
        try {
            val response =
                client.models.generateContent(
                    MODEL,
                    Content.fromParts(
                        Part.fromText("$TRANSCRIPT_PROMPT\n\n--- TRANSCRIPT ---\n$transcript"),
                    ),
                    null,
                )

            val rawText = response.text()
            logger.debug { "Gemini 응답 원문:\n$rawText" }
            val jsonText = stripMarkdownCodeBlock(rawText)
            val aiResponse = objectMapper.readValue(jsonText, AiApiResponse::class.java)
            logger.info { "Gemini 분석 결과: title=${aiResponse.title}, destination=${aiResponse.destination}" }
            return aiResponse.toDomain()
        } catch (e: LinktripException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Gemini AI 자막 분석 실패: videoId=$videoId" }
            throw LinktripException(ExceptionCode.BAD_GATEWAY_GEMINI)
        }
    }

    private fun analyzeFromVideo(youtubeUrl: String): VideoAnalysisResult {
        try {
            val response =
                client.models.generateContent(
                    MODEL,
                    Content.fromParts(
                        Part.fromText(VIDEO_PROMPT),
                        Part.fromUri(youtubeUrl, VIDEO_MIME_TYPE),
                    ),
                    null,
                )

            val jsonText = stripMarkdownCodeBlock(response.text())
            val aiResponse = objectMapper.readValue(jsonText, AiApiResponse::class.java)
            return aiResponse.toDomain()
        } catch (e: LinktripException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Gemini AI 영상 분석 실패: url=$youtubeUrl" }
            throw LinktripException(ExceptionCode.BAD_GATEWAY_GEMINI)
        }
    }

    companion object {
        private const val API_VERSION = "v1"
        private const val MODEL = "gemini-2.5-flash"
        private const val VIDEO_MIME_TYPE = "video/mp4"

        private fun extractVideoId(youtubeUrl: String): String {
            val patterns =
                listOf(
                    Regex("""[?&]v=([a-zA-Z0-9_-]{11})"""),
                    Regex("""youtu\.be/([a-zA-Z0-9_-]{11})"""),
                    Regex("""/shorts/([a-zA-Z0-9_-]{11})"""),
                    Regex("""/embed/([a-zA-Z0-9_-]{11})"""),
                )
            return patterns.firstNotNullOfOrNull { it.find(youtubeUrl)?.groupValues?.get(1) }
                ?: throw LinktripException(ExceptionCode.BAD_REQUEST_YOUTUBE_URL)
        }

        private fun formatTimestamp(totalSeconds: Long): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }

        private val TRANSCRIPT_PROMPT =
            """
            You are a travel video transcript analyzer. Return ONLY a raw JSON object (no markdown, no explanation).
            The following is a timestamped transcript from a YouTube travel video.

            If NOT travel-related → {"valid":false,"destination":null,"title":null,"summary":null,"estimatedMinCost":null,"estimatedMaxCost":null,"costBasis":null,"hashtags":null,"timeline":null,"days":null}

            If travel-related → extract all fields below:

            EXAMPLE:
            {"valid":true,"destination":"도쿄, 일본","title":"도쿄 3박 4일 여행","summary":"신주쿠에서 최고의 주말을 경험하세요.","estimatedMinCost":800000,"estimatedMaxCost":1500000,"costBasis":"VIDEO_MENTIONED","hashtags":["맛집여행","문화탐방"],"timeline":[{"timestampSeconds":0,"description":"인트로 & 시부야 도착"},{"timestampSeconds":135,"description":"시부야 스크램블 교차로"}],"days":[{"day":1,"items":[{"order":1,"category":"EAT","name":"이치란 라멘","description":"돈코츠 라멘","tips":"오픈 전 줄서기 추천"}]}]}

            FIELD RULES (all values in Korean, English brand names preserved):

            destination: "도시, 국가" format (e.g. "도쿄, 일본"). Multiple cities → primary city. Country-wide → country only. null if unknown.
            title: Natural Korean trip title, under 20 chars. MUST include trip duration as "N박 M일".
            TRIP DURATION RULES:
            1. Search the ENTIRE transcript (especially near the end: 정산, 마무리, 총 경비 sections) for trip duration mentions like "1박 2일", "2박 3일", "3박 4일", "4박 5일", "당일치기" etc.
            2. Even if the video only shows part of the trip (e.g. "둘째 날" only), the TOTAL trip duration mentioned in the transcript is what matters.
               Example: transcript says "둘째 날" content but mentions "1박 2일 상하이 여행 정산" → title should be "상하이 1박 2일 여행".
            3. If not explicitly mentioned, infer from day transitions and hotel nights.
            4. "당일치기" or single day = "당일" in title (e.g. "오사카 당일 여행").
            5. Title examples: "도쿄 3박 4일 여행", "상하이 1박 2일 미식여행", "오사카 당일 맛집투어".
            summary: 3-5 sentence Korean summary (max 300 chars). Focus on highlights, theme, vibe. null if unavailable.
            estimatedMinCost/estimatedMaxCost: KRW integer for 1 person.
            COST CALCULATION RULES:
            1. Look for total cost summaries in the transcript ("총 경비", "총 비용", "정산", "총 얼마", "만원").
            2. If a breakdown is found (e.g. 항공 40만 + 숙소 17만 + 식비 20만 + 교통 4만 + 기타 14만 = 총 95만):
               → EXCLUDE only international flight costs (항공편, 비행기값)
               → SUM everything else: lodging(숙소) + food(식비) + local transport(교통비) + activities + shopping + all other costs
               → Example: 총 95만 - 항공 40만 = 55만 → range: 49.5만 ~ 60.5만 (±10%)
            3. If no breakdown but total mentioned → subtract estimated flight cost, then ±10%.
            4. If no total but individual prices mentioned → sum all mentioned prices (costBasis="ITEM_ESTIMATED").
            5. costBasis: "VIDEO_MENTIONED" ONLY when using actual numbers from the transcript. "ITEM_ESTIMATED" when summing individual prices.
            6. You MUST provide estimates unless the transcript shows absolutely zero price information.
            hashtags: Up to 3 from: "맛집여행","SNS 핫플레이스","가성비여행","럭셔리여행","힐링여행","액티비티","문화탐방","쇼핑","자연경관","역사탐방","카페투어","야경명소","로컬맛집","온천여행","축제/이벤트". Empty array if none.
            timeline: 5-15 entries, chronological. Use timestamps from the transcript [M:SS] or [H:MM:SS]. Each: {"timestampSeconds": int, "description": Korean string under 30 chars}. null if timestamps undeterminable.
            days: Array of {"day": int, "items": [...]}.
            DAY NUMBERING RULES:
            1. ALWAYS start from day=1, regardless of what the transcript says.
               If the transcript says "둘째 날" or "2일차" but it's the first day shown in THIS video, use day=1.
            2. Infer day boundaries from context: "1일차", "둘째 날", "다음 날", morning/night transitions, hotel check-in/out.
            3. If the video covers only one day, use day=1.
            4. Count the number of distinct days shown in the video, then number them sequentially starting from 1.
               Example: Video shows "둘째 날" and "셋째 날" → day=1 and day=2 in our output.
            items: {"order": sequential int from 1, "category": string, "name": string, "description": string|null, "tips": string|null}

            CATEGORIES (exact strings):
            EAT: restaurants, cafes, street food, convenience store food (NOT airplane meals)
            ATTRACTION: landmarks, museums, temples, parks (NOT generic city names)
            SHOPPING: non-food stores, malls, duty-free (convenience store food → EAT)
            TRANSPORTATION_HUB: airports, train stations, bus terminals
            TRANSPORTATION_TRANSIT: subway/taxi/bus rides, passes (JR Pass, Suica)

            IMPORTANT: No consecutive duplicate places within a day (merge them). Romanize foreign names to Korean (ラーメン→라멘). Use null for missing description/tips.

            NOW ANALYZE THE TRANSCRIPT:
            """.trimIndent()

        private val VIDEO_PROMPT =
            """
            You are a travel video analyzer. Return ONLY a raw JSON object (no markdown, no explanation).

            If NOT travel-related → {"valid":false,"destination":null,"title":null,"summary":null,"estimatedMinCost":null,"estimatedMaxCost":null,"costBasis":null,"hashtags":null,"timeline":null,"days":null}

            If travel-related → extract all fields below:

            EXAMPLE:
            {"valid":true,"destination":"도쿄, 일본","title":"도쿄 3박 4일 여행","summary":"신주쿠에서 최고의 주말을 경험하세요. 숨겨진 명소와 야간 골목 문화까지 초점을 맞춥니다.","estimatedMinCost":800000,"estimatedMaxCost":1500000,"costBasis":"VIDEO_MENTIONED","hashtags":["맛집여행","문화탐방"],"timeline":[{"timestampSeconds":0,"description":"인트로 & 시부야 도착"},{"timestampSeconds":135,"description":"시부야 스크램블 교차로"},{"timestampSeconds":330,"description":"이치란 라멘 점심"}],"days":[{"day":1,"items":[{"order":1,"category":"EAT","name":"이치란 라멘","description":"돈코츠 라멘","tips":"오픈 전 줄서기 추천"}]}]}

            FIELD RULES (all values in Korean, English brand names preserved):

            destination: "도시, 국가" format (e.g. "도쿄, 일본"). Multiple cities → primary city. Country-wide → country only. null if unknown.
            title: Natural Korean trip title, under 20 chars (e.g. "도쿄 3박 4일 여행"). NOT "도시, 국가" format.
            summary: 3-5 sentence Korean summary (max 300 chars). Focus on highlights, theme, vibe. null if unavailable.
            estimatedMinCost/estimatedMaxCost: KRW integer for 1 person.
            COST CALCULATION RULES:
            1. Watch the END of the video carefully for total cost summaries ("총 경비", "총 비용", "정산", "총 얼마", price breakdown screens).
            2. If a breakdown is shown (e.g. 항공 40만 + 숙소 17만 + 식비 20만 + 교통 4만 + 기타 14만 = 총 95만):
               → EXCLUDE only international flight costs (항공편, 비행기값)
               → SUM everything else: lodging(숙소) + food(식비) + local transport(교통비) + activities + shopping + all other costs
               → Example: 총 95만 - 항공 40만 = 55만 → range: 49.5만 ~ 60.5만 (±10%)
            3. If no breakdown but total mentioned → subtract estimated flight cost, then ±10%.
            4. If no total but individual prices shown → sum all shown prices (costBasis="ITEM_ESTIMATED").
            5. costBasis: "VIDEO_MENTIONED" ONLY when using actual numbers from the video. "ITEM_ESTIMATED" when summing individual prices.
            6. You MUST provide estimates unless the video shows absolutely zero price information.
            hashtags: Up to 3 from: "맛집여행","SNS 핫플레이스","가성비여행","럭셔리여행","힐링여행","액티비티","문화탐방","쇼핑","자연경관","역사탐방","카페투어","야경명소","로컬맛집","온천여행","축제/이벤트". Empty array if none.
            timeline: 5-15 entries, chronological. Each: {"timestampSeconds": int, "description": Korean string under 30 chars}. Start near 0s. null if timestamps undeterminable.
            days: Array of {"day": int, "items": [...]}. Infer days from context ("1일차", morning/night transitions). Single day if unclear.
            items: {"order": sequential int from 1, "category": string, "name": string, "description": string|null, "tips": string|null}

            CATEGORIES (exact strings):
            EAT: restaurants, cafes, street food, convenience store food (NOT airplane meals)
            ATTRACTION: landmarks, museums, temples, parks (NOT generic city names)
            SHOPPING: non-food stores, malls, duty-free (convenience store food → EAT)
            TRANSPORTATION_HUB: airports, train stations, bus terminals
            TRANSPORTATION_TRANSIT: subway/taxi/bus rides, passes (JR Pass, Suica)

            IMPORTANT: No consecutive duplicate places within a day (merge them). Romanize foreign names to Korean (ラーメン→라멘). Use null for missing description/tips.

            NOW ANALYZE THE VIDEO:
            """.trimIndent()

        private fun stripMarkdownCodeBlock(text: String?): String {
            if (text.isNullOrBlank()) return "{}"
            val trimmed = text.trim()
            if (!trimmed.startsWith("`")) return trimmed
            return trimmed
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }
    }
}
