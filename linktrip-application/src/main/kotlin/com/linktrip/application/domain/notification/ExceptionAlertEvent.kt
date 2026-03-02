package com.linktrip.application.domain.notification

import java.time.LocalDateTime

data class ExceptionAlertEvent(
    val message: String,
    val cause: String?,
    val statusCode: Int,
    val stackTrace: String?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
