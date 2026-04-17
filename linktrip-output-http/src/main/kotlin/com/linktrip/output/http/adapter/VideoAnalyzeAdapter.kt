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
import io.github.thoroldvix.api.TranscriptRetrievalException
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
    private val transcriptClient: YoutubeTranscriptClient,
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

    @PreDestroy
    fun close() {
        runCatching { client.close() }
            .onFailure { logger.warn(it) { "Gemini Client 종료 중 에러 발생" } }
    }

    override fun extractTranscript(youtubeUrl: String): String {
        val videoId = extractVideoId(youtubeUrl)
        val transcript = tryExtractTranscript(videoId)

        if (transcript == null) {
            logger.warn { "자막 없음, 분석 불가: videoId=$videoId" }
            throw LinktripException(ExceptionCode.BAD_REQUEST_VIDEO, "자막을 추출할 수 없는 영상입니다.")
        }

        logger.info { "자막 추출 완료: videoId=$videoId (${transcript.length}자)" }
        logger.debug { "자막 프리뷰: ${transcript.take(TRANSCRIPT_PREVIEW_LENGTH).replace("\n", " ")}..." }
        return transcript
    }

    override fun analyzeFromTranscript(
        transcript: String,
        youtubeUrl: String,
    ): VideoAnalysisResult {
        val videoId = extractVideoId(youtubeUrl)
        return analyzeFromTranscriptInternal(transcript, videoId)
    }

    private fun tryExtractTranscript(videoId: String): String? =
        try {
            transcriptClient.fetchTranscript(videoId)
        } catch (e: TranscriptRetrievalException) {
            // 라이브러리가 우리 LinktripException 을 한 단계 감쌌을 수 있어 cause 를 우선 확인.
            (e.cause as? LinktripException)?.let { throw it }
            classifyAmbiguousFailure(videoId, e)
        } catch (e: IllegalArgumentException) {
            logger.warn { "videoId 형식 오류 (videoId=$videoId): ${e.message}" }
            null
        }

    /**
     * 모호한 자막 실패 → sentinel ping 으로 "프록시 죽음" vs "영상 고유 문제" 분류.
     */
    private fun classifyAmbiguousFailure(
        videoId: String,
        cause: TranscriptRetrievalException,
    ): String? {
        if (transcriptClient.isProxyHealthy()) {
            logger.warn(cause) { "Sentinel 정상 → 자막 없음/영상 접근 불가 (videoId=$videoId)" }
            return null
        }
        logger.warn(cause) { "Sentinel 실패 → IP 차단 의심 (videoId=$videoId)" }
        throw LinktripException(
            ExceptionCode.BAD_GATEWAY_YOUTUBE,
            "Sentinel 확인 결과 IP 차단 의심.",
        )
    }

    private fun analyzeFromTranscriptInternal(
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
            logger.debug { "Gemini 응답 길이: ${rawText?.length ?: 0}자" }
            val jsonText = stripMarkdownCodeBlock(rawText)
            val aiResponse =
                try {
                    objectMapper.readValue(jsonText, AiApiResponse::class.java)
                } catch (e: Exception) {
                    logger.error { "Gemini 응답 JSON 파싱 실패: videoId=$videoId, raw=$rawText" }
                    throw e
                }
            logger.info {
                "Gemini 분석 결과: videoId=$videoId, title=${aiResponse.title}, " +
                    "destination=${aiResponse.destination}, " +
                    "cost=${aiResponse.estimatedMinCost}~${aiResponse.estimatedMaxCost}"
            }
            if (aiResponse.valid && (aiResponse.estimatedMinCost == null || aiResponse.estimatedMaxCost == null)) {
                logger.warn { "가격 추출 실패(프롬프트 개선 필요): videoId=$videoId, title=${aiResponse.title}" }
            }
            return aiResponse.toDomain()
        } catch (e: LinktripException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Gemini AI 자막 분석 실패: videoId=$videoId" }
            throw LinktripException(ExceptionCode.BAD_GATEWAY_GEMINI)
        }
    }

    /**
     * 자막 없이 Gemini 가 YouTube 영상 자체를 직접 인제스트해 분석하는 fallback.
     *
     * 현재 미사용:
     * - 자막 기반 분석 대비 토큰 소비량이 약 25배에 달해 비용이 크게 증가한다.
     * - 자막이 추출되는 영상은 [analyzeFromTranscript] 가 먼저 처리하므로 호출 경로가 닿지 않는다.
     *
     * 자막 추출 자체가 불가능한 영상에 대한 fallback 으로 와이어업할 경우에만 사용한다.
     */
    private fun analyzeByAiVideoIngestion(youtubeUrl: String): VideoAnalysisResult {
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
        private const val TRANSCRIPT_PREVIEW_LENGTH = 150

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
            estimatedMinCost/estimatedMaxCost: KRW integer for 1 person. NEVER null — always provide a range.
            COST CALCULATION RULES (try in order, stop at first applicable):
            1. Total cost with breakdown in transcript (e.g. 항공 40만 + 숙소 17만 + 식비 20만 + 교통 4만 + 기타 14만 = 총 95만):
               → EXCLUDE only international flight costs (항공편, 비행기값)
               → SUM everything else: lodging(숙소) + food(식비) + local transport(교통비) + activities + shopping + etc.
               → Apply ±10% range. Example: 55만 → 49.5만 ~ 60.5만. costBasis="VIDEO_MENTIONED".
            2. Total mentioned without breakdown → subtract estimated flight cost (country-dependent), then ±10%. costBasis="VIDEO_MENTIONED".
            3. Individual prices mentioned but no total → sum all mentioned prices, ±15%. costBasis="ITEM_ESTIMATED".
            4. NO price info at all → ESTIMATE per-place using your real-world knowledge of each specific venue:
               For EACH item in the itinerary (by name), determine the typical spend based on that venue's actual pricing:
                 - EAT: the signature/popular menu price at that specific restaurant.
                   Examples: 이치란 라멘 → 라멘 세트 약 12,000원 / 스시로 → 회전초밥 2인당 약 10,000원 /
                            블루보틀 도쿄 → 커피 약 7,000원 / 츠케멘 츠지타 → 츠케멘 약 13,000원.
                   If restaurant is unknown or generic (e.g. "현지 카페"), use cuisine-type average
                   (라멘 12,000 / 스시 25,000 / 카페 8,000 / 스트릿푸드 5,000 / 파인다이닝 80,000).
                 - ATTRACTION: the actual entry/ticket fee for that landmark.
                   Examples: 도쿄타워 전망대 약 18,000원 / 유니버설 스튜디오 재팬 약 95,000원 /
                            센소지(사찰) 무료 / 에펠탑 전망대 약 42,000원 / 루브르 박물관 약 30,000원.
                   Unknown attractions: park/temple 0 / museum 15,000 / observation deck 20,000 / theme park 90,000.
                 - SHOPPING: typical per-visit spend at that store type.
                   Examples: 돈키호테 방문 시 약 50,000원 / 아웃렛 방문 시 약 150,000원 /
                            편의점 간식 약 5,000원 / 기념품샵 약 20,000원.
                 - TRANSPORTATION_HUB: 0 (airports/stations are waypoints, no spend).
                 - TRANSPORTATION_TRANSIT: actual fare/pass price.
                   Examples: JR Pass 7일권 약 350,000원 / Suica 충전 약 30,000원 /
                            도쿄 지하철 1회 약 2,500원 / 택시 1회 약 15,000원.
               Then add LODGING per night based on the destination and what you infer from the itinerary quality
               (business/호스텔 60,000 / 일반 비즈니스호텔 100,000 / 중급 150,000 / 고급·리조트 250,000),
               multiplied by (일수 - 1) if 당일치기가 아니면, else 0.
               SUM all per-place estimates + lodging. Apply ±20% range. costBasis="ITEM_ESTIMATED".
               IMPORTANT: The result should be a MEANINGFUL total that reflects the actual venues visited,
               not generic averages. If a venue is clearly a high-end place, use its actual higher price point.
            5. Sanity check: final min must be ≥ 50,000 (당일치기 최소). If lower, re-check — you likely missed items or used too-low estimates.
            6. NEVER return null for estimatedMinCost/estimatedMaxCost. At minimum use rule 4.
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
