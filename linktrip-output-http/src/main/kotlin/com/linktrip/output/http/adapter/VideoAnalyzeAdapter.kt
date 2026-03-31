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
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.FileInputStream
import javax.annotation.PreDestroy

private val logger = KotlinLogging.logger {}

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

    @PreDestroy
    fun close() {
        runCatching { client.close() }
            .onFailure { logger.warn(it) { "Gemini Client 종료 중 에러 발생" } }
    }

    override fun analyze(youtubeUrl: String): VideoAnalysisResult {
        try {
            val response =
                client.models.generateContent(
                    MODEL,
                    Content.fromParts(
                        Part.fromText(DEFAULT_PROMPT),
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
            logger.error(e) { "Gemini AI 분석 실패: url=$youtubeUrl" }
            throw LinktripException(ExceptionCode.BAD_GATEWAY_GEMINI)
        }
    }

    companion object {
        private const val API_VERSION = "v1"
        private const val MODEL = "gemini-2.5-flash"
        private const val VIDEO_MIME_TYPE = "video/mp4"
        private val DEFAULT_PROMPT =
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
            estimatedMinCost/estimatedMaxCost: KRW integer for 1 person. Include food/lodging/transport/activities. Exclude international flights. Priority: video-mentioned cost (±10%, costBasis="VIDEO_MENTIONED") > sum of shown prices > estimated sum (costBasis="ITEM_ESTIMATED"). null if unestimable.
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
