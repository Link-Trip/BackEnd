package com.linktrip.input.http.ratelimit

import com.linktrip.application.port.output.ratelimit.RateLimitBucketStore
import com.linktrip.application.port.output.ratelimit.RateLimitPolicy
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.security.PostAuthorizationToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

@ExtendWith(MockitoExtension::class)
class RateLimitInterceptorTest {
    @Mock
    lateinit var rateLimitBucketStore: RateLimitBucketStore

    @InjectMocks
    lateinit var interceptor: RateLimitInterceptor

    private fun setAuthentication(memberId: String) {
        SecurityContextHolder.getContext().authentication = PostAuthorizationToken(memberId)
    }

    private fun clearAuthentication() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `인증된 사용자의 요청이 rate limit 이내이면 통과한다`() {
        // given
        val request = MockHttpServletRequest("POST", "/video/analyze")
        setAuthentication("member1")
        whenever(rateLimitBucketStore.tryConsume(eq("member1:VIDEO_ANALYZE"), eq(RateLimitPolicy.VIDEO_ANALYZE)))
            .thenReturn(true)

        // when
        val result = interceptor.preHandle(request, MockHttpServletResponse(), Any())

        // then
        assertTrue(result)
        clearAuthentication()
    }

    @Test
    fun `rate limit을 초과하면 LinktripException(RATE_LIMIT_EXCEEDED)을 던진다`() {
        // given
        val request = MockHttpServletRequest("POST", "/video/analyze")
        setAuthentication("member1")
        whenever(rateLimitBucketStore.tryConsume(eq("member1:VIDEO_ANALYZE"), eq(RateLimitPolicy.VIDEO_ANALYZE)))
            .thenReturn(false)

        // when & then
        val exception =
            assertThrows<LinktripException> {
                interceptor.preHandle(request, MockHttpServletResponse(), Any())
            }
        assertEquals(ExceptionCode.TOO_MANY_REQUESTS.statusCode, exception.statusCode)
        clearAuthentication()
    }

    @Test
    fun `인증되지 않은 요청은 rate limit 체크 없이 통과한다`() {
        // given
        clearAuthentication()
        val request = MockHttpServletRequest("POST", "/video/analyze")

        // when
        val result = interceptor.preHandle(request, MockHttpServletResponse(), Any())

        // then - bucketStore 호출 없이 통과
        assertTrue(result)
        verify(rateLimitBucketStore, never()).tryConsume(any(), any())
    }

    @Test
    fun `일반 경로도 DEFAULT 정책으로 rate limit이 적용된다`() {
        // given
        val request = MockHttpServletRequest("GET", "/video/abc123/schedule")
        setAuthentication("member1")
        whenever(rateLimitBucketStore.tryConsume(eq("member1:DEFAULT"), eq(RateLimitPolicy.DEFAULT)))
            .thenReturn(true)

        // when
        val result = interceptor.preHandle(request, MockHttpServletResponse(), Any())

        // then
        assertTrue(result)
        verify(rateLimitBucketStore).tryConsume(eq("member1:DEFAULT"), eq(RateLimitPolicy.DEFAULT))
        clearAuthentication()
    }
}
