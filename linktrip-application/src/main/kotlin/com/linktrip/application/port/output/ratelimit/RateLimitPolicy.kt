package com.linktrip.application.port.output.ratelimit

import java.time.Duration

enum class RateLimitPolicy(
    val capacity: Long,
    val refillTokens: Long,
    val refillDuration: Duration,
) {
    VIDEO_ANALYZE(3, 3, Duration.ofHours(1)),
    KEYWORD_ANALYZE(5, 5, Duration.ofHours(1)),
    DISCOVER(30, 30, Duration.ofMinutes(1)),
    DEFAULT(60, 60, Duration.ofMinutes(1)),
}
