package com.linktrip.application.domain.log

import java.time.LocalDateTime

data class AccessLog(
    val requestId: String,
    val method: String,
    val uri: String,
    val clientIp: String,
    val statusCode: Int,
    val durationMs: Long,
    val errorMessage: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
