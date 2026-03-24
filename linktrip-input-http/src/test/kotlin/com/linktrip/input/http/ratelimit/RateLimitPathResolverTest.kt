package com.linktrip.input.http.ratelimit

import com.linktrip.application.port.output.ratelimit.RateLimitPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RateLimitPathResolverTest {
    @Test
    fun `영상 분석 경로는 VIDEO_ANALYZE 정책을 반환한다`() {
        // when
        val result = RateLimitPathResolver.resolve("/video/analyze")

        // then
        assertEquals(RateLimitPolicy.VIDEO_ANALYZE, result)
    }

    @Test
    fun `디스커버 경로는 DEFAULT 정책을 반환한다`() {
        // when
        val result = RateLimitPathResolver.resolve("/video/discover/category")

        // then
        assertEquals(RateLimitPolicy.DEFAULT, result)
    }

    @Test
    fun `일정 조회 경로는 DEFAULT 정책을 반환한다`() {
        // when
        val result = RateLimitPathResolver.resolve("/video/abc123/schedule")

        // then
        assertEquals(RateLimitPolicy.DEFAULT, result)
    }

    @Test
    fun `인증 경로도 DEFAULT 정책을 반환한다`() {
        // when
        val result = RateLimitPathResolver.resolve("/auth/login")

        // then
        assertEquals(RateLimitPolicy.DEFAULT, result)
    }
}
