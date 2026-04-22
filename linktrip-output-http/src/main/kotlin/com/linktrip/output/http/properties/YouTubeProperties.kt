package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "youtube")
data class YouTubeProperties(
    val apiKey: String,
    val proxy: ProxyProperties = ProxyProperties(),
    val healthCheck: HealthCheckProperties = HealthCheckProperties(),
) {
    data class ProxyProperties(
        val password: String = "",
        val usernames: List<String> = emptyList(),
    ) {
        fun isEnabled(): Boolean = password.isNotBlank() && usernames.isNotEmpty()
    }

    data class HealthCheckProperties(
        val sentinelVideoId: String = "",
    )
}
