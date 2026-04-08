package com.linktrip.application.port.output.idempotency

data class CachedResponse(
    val status: IdempotencyStatus,
    val body: Any? = null,
)
