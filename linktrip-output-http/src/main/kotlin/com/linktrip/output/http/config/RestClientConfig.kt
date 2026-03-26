package com.linktrip.output.http.config

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Configuration
class RestClientConfig {
    @Bean("googlePlacesRestClient")
    fun googlePlacesRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(GOOGLE_PLACES_BASE_URL)
            .defaultHeader("X-Goog-FieldMask", GOOGLE_PLACES_FIELD_MASK)
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "Google Places API 호출 실패: method=${request.method} status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.BAD_GATEWAY_GOOGLE_PLACES)
            }
            .requestFactory(clientHttpRequestFactory(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build()

    @Bean("discordRestClient")
    fun discordRestClient(): RestClient =
        RestClient.builder()
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "Discord API 호출 실패: method=${request.method} status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.BAD_GATEWAY_DISCORD)
            }
            .requestFactory(clientHttpRequestFactory(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build()

    @Bean("youtubeRestClient")
    fun youtubeRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(YOUTUBE_BASE_URL)
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "YouTube API 호출 실패: method=${request.method} status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.BAD_GATEWAY_YOUTUBE)
            }
            .requestFactory(clientHttpRequestFactory(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build()

    private fun clientHttpRequestFactory(
        connectTimeout: Duration,
        readTimeout: Duration,
    ) = SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(connectTimeout)
        setReadTimeout(readTimeout)
    }

    companion object {
        private const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/v1"
        private const val GOOGLE_PLACES_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location"
        private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private val CONNECT_TIMEOUT = Duration.ofSeconds(5)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
    }
}
