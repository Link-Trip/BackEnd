package com.linktrip.application.port.output.ratelimit

import java.time.Duration

enum class RateLimitPolicy(
    val capacity: Long,
    val refillTokens: Long,
    val refillDuration: Duration,
    val initialTokens: Long = capacity,
) {
    VIDEO_ANALYZE(3, 3, Duration.ofHours(1)),
    GEMINI_API(12, 12, Duration.ofMinutes(1), initialTokens = 0),
    DEFAULT(60, 60, Duration.ofMinutes(1)),
}
