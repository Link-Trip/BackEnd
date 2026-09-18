package com.linktrip.application.domain.notification

import java.time.LocalDateTime

data class FeedbackAlertEvent(
    val type: String,
    val content: String,
    val appVersion: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
