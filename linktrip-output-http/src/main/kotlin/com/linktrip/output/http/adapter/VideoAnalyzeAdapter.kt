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
            You are a travel video content analyzer specialized in extracting structured travel itineraries.

            ============================================================
            CRITICAL: YOUR RESPONSE MUST BE ONLY A VALID JSON OBJECT
            ============================================================
            - NO ```json``` markdown blocks
            - NO text before or after the JSON
            - NO explanations or comments
            - ONLY the raw JSON object

            ============================================================
            STEP 1: CONTENT VALIDATION
            ============================================================
            Is this video travel-related? (travel vlog, food tour, sightseeing, travel tips, etc.)

            If NO (violence, adult content, unrelated topics, invalid link):
            → Return: {"valid": false, "destination": null, "days": null}

            If YES → Proceed to STEP 2

            ============================================================
            STEP 2: EXTRACT DESTINATION AND DAY-BY-DAY ITINERARY
            ============================================================
            First, identify the main travel destination from the video.
            Then extract the travel schedule in CHRONOLOGICAL ORDER, grouped by day.
            Each item must have a category tag and appear in the order it was visited.

            {
              "valid": true,
              "destination": "도쿄, 일본",
              "days": [
                {
                  "day": 1,
                  "items": [
                    {"order": 1, "category": "TRANSPORTATION", "name": "나리타 익스프레스", "description": "공항에서 도쿄역까지 이동", "tips": "JR패스 사용 가능"},
                    {"order": 2, "category": "EAT", "name": "이치란 라멘", "description": "돈코츠 라멘", "tips": "오픈 전 줄서기 추천"},
                    {"order": 3, "category": "ATTRACTION", "name": "센소지", "description": "아사쿠사의 대표 사찰", "tips": null},
                    {"order": 4, "category": "SHOPPING", "name": "돈키호테", "description": "과자, 화장품 구매", "tips": "면세 가능"}
                  ]
                },
                {
                  "day": 2,
                  "items": [...]
                }
              ]
            }

            ============================================================
            DESTINATION RULES
            ============================================================
            - Extract the main travel destination in format: "도시, 국가" (e.g., "도쿄, 일본", "방콕, 태국", "파리, 프랑스")
            - If multiple cities are visited, use the primary/most-visited city
            - If the video covers an entire country or region, use "국가" only (e.g., "일본", "태국")
            - Write in Korean
            - "destination" is either: "도시, 국가" format, a country-only string when no single city is dominant, or null

            ============================================================
            DAY DETECTION RULES
            ============================================================
            - If the video mentions "1일차", "첫째 날", "Day 1" etc., follow that structure
            - If not explicitly mentioned, infer days from context (morning/night transitions, hotel check-in, sleeping scenes)
            - If the video covers a single day or cannot determine days, use "day": 1 for all items
            - order must be sequential within each day starting from 1

            ============================================================
            CATEGORY VALUES (use EXACTLY these strings)
            ============================================================
            - "EAT": Restaurants, cafes, bars, street food, convenience store food, food markets
                     EXCLUDE: Airplane meals, in-flight food
            - "ATTRACTION": Specific landmarks, museums, temples, parks, beaches
                            EXCLUDE: Generic city names like "Seoul", "Tokyo", "Paris"
            - "SHOPPING": Stores for NON-FOOD items (clothes, souvenirs, cosmetics), malls, duty-free
                          EXCLUDE: Food at convenience store → use "EAT"
            - "TRANSPORTATION": Train, bus, taxi, rental car, passes (JR Pass, Suica, T-money), routes

            ============================================================
            DEDUPLICATION RULES
            ============================================================
            - NEVER repeat the same place in consecutive orders within a day
            - If the video mentions the same place multiple times in sequence, merge into ONE item
            - Combine all relevant tips and descriptions into the single merged item
            - If a place is revisited later after other items on the same day, keep it as a separate item
            - If a place is genuinely revisited on a DIFFERENT day, it may appear again

            ============================================================
            LANGUAGE RULES
            ============================================================
            - Write all values in Korean
            - Keep English brand names in English (e.g., "Shake Shack", "Starbucks", "7-Eleven")
            - Romanize Japanese/Chinese/Thai/Vietnamese to Korean pronunciation
              (e.g., ラーメン → "라멘", 火鍋 → "훠궈", センソジ → "센소지")
            - NO timestamps
            - Use null for missing description or tips

            ============================================================
            OUTPUT VALIDATION CHECKLIST
            ============================================================
            Before responding, verify:
            - Response is ONLY a JSON object (no other text)
            - "destination" is a string in "도시, 국가" format, a country-only string, or null
            - "days" is an array of day objects, each with "day" (int) and "items" (array)
            - Each item has: order (int), category (string), name (string), description (string or null), tips (string or null)
            - category is one of: EAT, ATTRACTION, SHOPPING, TRANSPORTATION
            - Items are in chronological visit order within each day
            - Convenience store food is "EAT", not "SHOPPING"
            - No airplane meals included
            - No generic city names in attractions
            - No duplicate names in consecutive orders within the same day

            NOW ANALYZE THE VIDEO AND RETURN THE JSON:
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
