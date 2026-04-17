package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "youtube")
data class YouTubeProperties(
    val apiKey: String,
    val proxy: ProxyProperties = ProxyProperties(),
    val healthCheck: HealthCheckProperties = HealthCheckProperties(),
) {
    data class ProxyProperties(
        val username: String = "",
        val password: String = "",
    ) {
        fun isEnabled(): Boolean = username.isNotBlank() && password.isNotBlank()
    }

    data class HealthCheckProperties(
        val sentinelVideoId: String = "",
    )
}
