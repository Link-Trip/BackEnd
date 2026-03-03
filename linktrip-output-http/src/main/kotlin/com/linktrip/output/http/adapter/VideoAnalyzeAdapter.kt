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

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalyzeAdapter(
    private val gcpProperties: GcpProperties,
    private val objectMapper: ObjectMapper,
) : VideoAnalyzePort {
    override fun analyze(youtubeUrl: String): VideoAnalysisResult {
        try {
            newClient().use { client ->
                val response =
                    client.models.generateContent(
                        MODEL,
                        Content.fromParts(
                            Part.fromText(DEFAULT_PROMPT),
                            Part.fromUri(youtubeUrl, VIDEO_MIME_TYPE),
                        ),
                        null,
                    )

                val jsonText = response.text()
                val aiResponse = objectMapper.readValue(jsonText, AiApiResponse::class.java)
                return aiResponse.toDomain()
            }
        } catch (e: LinktripException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Gemini AI 분석 실패: url=$youtubeUrl" }
            throw LinktripException(ExceptionCode.EXTERNAL_API_ERROR)
        }
    }

    private fun newClient(): Client {
        val credentials =
            GoogleCredentials.fromStream(FileInputStream(gcpProperties.credentialsPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
        return Client
            .builder()
            .vertexAI(true)
            .project(gcpProperties.projectId)
            .location(gcpProperties.vertexAi.location)
            .httpOptions(HttpOptions.builder().apiVersion(API_VERSION).build())
            .credentials(credentials)
            .build()
    }

    companion object {
        private const val API_VERSION = "v1"
        private const val MODEL = "gemini-2.5-flash"
        private const val VIDEO_MIME_TYPE = "video/mp4"
        private val DEFAULT_PROMPT =
            """
            You are a travel video content analyzer specialized in extracting structured travel information.

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
            → Return: {"valid": false, "eats": null, "attractions": null, "shoppings": null, "transportations": null}

            If YES → Proceed to STEP 2

            ============================================================
            STEP 2: EXTRACT DATA INTO THIS EXACT JSON STRUCTURE
            ============================================================
            {
              "valid": true,
              "eats": [
                {
                  "restaurant": "식당/가게명",
                  "food": "먹은 음식",
                  "restaurantsAndFoodsTips": "꿀팁 또는 null"
                }
              ],
              "attractions": [
                {
                  "attractions": "관광지명",
                  "attractionsTips": "꿀팁 또는 null"
                }
              ],
              "shoppings": [
                {
                  "shopping": "쇼핑장소명",
                  "shoppingTips": "꿀팁 또는 null"
                }
              ],
              "transportations": [
                {
                  "transportation": "교통수단/방법",
                  "transportationTips": "꿀팁 또는 null"
                }
              ]
            }

            ============================================================
            CATEGORY CLASSIFICATION RULES (IMPORTANT!)
            ============================================================

            EATS - Include these:
               Restaurants, cafes, bars
               Convenience store food → eats, NOT shopping!
               Street food vendors
               Food markets where they ATE something
               EXCLUDE: Airplane meals, in-flight food

            ATTRACTIONS - Include these:
               Specific landmarks (e.g., "센소지", "Tokyo Skytree", "남산타워")
               Museums, temples, parks, beaches
               EXCLUDE: Generic city names like "Seoul", "Tokyo", "Paris"

            SHOPPINGS - Include these:
               Stores where they bought NON-FOOD items (clothes, souvenirs, cosmetics)
               Shopping malls, duty-free shops
               EXCLUDE: If they bought FOOD at convenience store → goes to EATS

            TRANSPORTATIONS - Include these:
               Transportation methods (train, bus, taxi, rental car)
               Transportation passes/cards (JR Pass, Suica, T-money)
               Specific routes or lines

            ============================================================
            LANGUAGE RULES
            ============================================================
            - Write all values in Korean
            - Keep English brand names in English (e.g., "Shake Shack", "Starbucks", "7-Eleven")
            - Romanize Japanese/Chinese/Thai/Vietnamese to Korean pronunciation
              (e.g., ラーメン → "라멘", 火鍋 → "훠궈", センソジ → "센소지")
            - NO timestamps
            - Use null for empty arrays or missing tips

            ============================================================
            OUTPUT VALIDATION CHECKLIST
            ============================================================
            Before responding, verify:
            - Response is ONLY a JSON object (no other text)
            - All keys match exactly: valid, eats, attractions, shoppings, transportations
            - Nested objects have correct keys
            - Convenience store food is in "eats", not "shoppings"
            - No airplane meals included
            - No generic city names in attractions

            NOW ANALYZE THE VIDEO AND RETURN THE JSON:
            """.trimIndent()
    }
}
