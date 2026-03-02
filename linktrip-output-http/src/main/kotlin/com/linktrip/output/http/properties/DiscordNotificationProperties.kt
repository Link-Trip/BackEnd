package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notification.discord")
data class DiscordNotificationProperties(
    val webhookUrlError: String,
    val mentionUserId: String,
)
