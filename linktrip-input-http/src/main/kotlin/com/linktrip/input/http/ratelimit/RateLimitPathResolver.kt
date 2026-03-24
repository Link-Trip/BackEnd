package com.linktrip.input.http.ratelimit

import com.linktrip.application.port.output.ratelimit.RateLimitPolicy

object RateLimitPathResolver {
    fun resolve(uri: String): RateLimitPolicy? =
        when {
            uri.startsWith("/video/analyze") -> RateLimitPolicy.VIDEO_ANALYZE
            uri.startsWith("/video/keyword") -> RateLimitPolicy.KEYWORD_ANALYZE
            uri.startsWith("/video/discover") -> RateLimitPolicy.DISCOVER
            else -> null
        }
}
