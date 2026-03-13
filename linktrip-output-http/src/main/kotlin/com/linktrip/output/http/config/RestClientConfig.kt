package com.linktrip.output.http.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class RestClientConfig {
    @Bean("googlePlacesRestClient")
    fun googlePlacesRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(GOOGLE_PLACES_BASE_URL)
            .defaultHeader("X-Goog-FieldMask", GOOGLE_PLACES_FIELD_MASK)
            .requestFactory(clientHttpRequestFactory(CONNECT_TIMEOUT, READ_TIMEOUT))
            .build()

    @Bean("discordRestClient")
    fun discordRestClient(): RestClient =
        RestClient.builder()
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
        private val CONNECT_TIMEOUT = Duration.ofSeconds(5)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
    }
}
