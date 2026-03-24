package com.linktrip.input.http.ratelimit

import com.linktrip.application.port.output.ratelimit.RateLimitPolicy

object RateLimitPathResolver {
    fun resolve(uri: String): RateLimitPolicy? =
        when {
            uri.startsWith("/video/analyze") -> RateLimitPolicy.VIDEO_ANALYZE
            else -> RateLimitPolicy.DEFAULT
        }
}
