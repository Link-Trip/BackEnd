package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.port.output.ratelimit.RateLimitPolicy
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryBucketStoreTest {
    private lateinit var bucketStore: InMemoryBucketStore

    @BeforeEach
    fun setUp() {
        bucketStore = InMemoryBucketStore()
    }

    @Test
    fun `capacity 이내의 요청은 허용된다`() {
        // given - VIDEO_ANALYZE 정책 (시간당 3회)
        val key = "member1:VIDEO_ANALYZE"

        // when & then - 3회까지 허용
        assertTrue(bucketStore.tryConsumeOrReject(key, RateLimitPolicy.VIDEO_ANALYZE))
        assertTrue(bucketStore.tryConsumeOrReject(key, RateLimitPolicy.VIDEO_ANALYZE))
        assertTrue(bucketStore.tryConsumeOrReject(key, RateLimitPolicy.VIDEO_ANALYZE))
    }

    @Test
    fun `capacity를 초과하면 요청이 거부된다`() {
        // given - VIDEO_ANALYZE 정책 (시간당 3회)을 모두 소진
        val key = "member1:VIDEO_ANALYZE"
        repeat(3) { bucketStore.tryConsumeOrReject(key, RateLimitPolicy.VIDEO_ANALYZE) }

        // when - 4번째 요청
        val result = bucketStore.tryConsumeOrReject(key, RateLimitPolicy.VIDEO_ANALYZE)

        // then - 거부된다
        assertFalse(result)
    }

    @Test
    fun `서로 다른 사용자는 독립적으로 rate limit이 적용된다`() {
        // given - member1이 capacity를 모두 소진
        val key1 = "member1:VIDEO_ANALYZE"
        val key2 = "member2:VIDEO_ANALYZE"
        repeat(3) { bucketStore.tryConsumeOrReject(key1, RateLimitPolicy.VIDEO_ANALYZE) }

        // when - member2가 요청
        val result = bucketStore.tryConsumeOrReject(key2, RateLimitPolicy.VIDEO_ANALYZE)

        // then - member2는 허용된다
        assertTrue(result)
    }

    @Test
    fun `서로 다른 정책은 독립적으로 rate limit이 적용된다`() {
        // given - VIDEO_ANALYZE capacity를 모두 소진
        val analyzeKey = "member1:VIDEO_ANALYZE"
        val defaultKey = "member1:DEFAULT"
        repeat(3) { bucketStore.tryConsumeOrReject(analyzeKey, RateLimitPolicy.VIDEO_ANALYZE) }

        // when - 같은 사용자가 DEFAULT 정책으로 요청
        val result = bucketStore.tryConsumeOrReject(defaultKey, RateLimitPolicy.DEFAULT)

        // then - 다른 정책이므로 허용된다
        assertTrue(result)
    }
}
