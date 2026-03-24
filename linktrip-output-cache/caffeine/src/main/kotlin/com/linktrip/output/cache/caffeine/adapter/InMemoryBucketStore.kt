package com.linktrip.output.cache.caffeine.adapter

import com.github.benmanes.caffeine.cache.Caffeine
import com.linktrip.application.port.output.ratelimit.RateLimitBucketStore
import com.linktrip.application.port.output.ratelimit.RateLimitPolicy
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class InMemoryBucketStore : RateLimitBucketStore {
    private val buckets =
        Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(MAX_BUCKET_SIZE)
            .build<String, Bucket>()

    override fun tryConsume(
        key: String,
        policy: RateLimitPolicy,
    ): Boolean {
        val bucket = buckets.get(key) { createBucket(policy) }
        return bucket.tryConsume(1)
    }

    private fun createBucket(policy: RateLimitPolicy): Bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(policy.capacity)
                    .refillGreedy(policy.refillTokens, policy.refillDuration)
                    .build(),
            )
            .build()

    companion object {
        private const val MAX_BUCKET_SIZE = 10_000L
    }
}
