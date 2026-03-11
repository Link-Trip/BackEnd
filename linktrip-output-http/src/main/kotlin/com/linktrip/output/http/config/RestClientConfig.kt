package com.linktrip.output.http.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean("googlePlacesRestClient")
    fun googlePlacesRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(GOOGLE_PLACES_BASE_URL)
            .defaultHeader("X-Goog-FieldMask", GOOGLE_PLACES_FIELD_MASK)
            .build()

    @Bean("discordRestClient")
    fun discordRestClient(): RestClient =
        RestClient.builder()
            .build()

    companion object {
        private const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/v1"
        private const val GOOGLE_PLACES_FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location"
    }
}
