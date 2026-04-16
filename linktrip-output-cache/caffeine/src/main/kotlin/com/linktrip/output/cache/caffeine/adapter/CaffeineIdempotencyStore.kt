package com.linktrip.output.cache.caffeine.adapter

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.linktrip.application.port.output.idempotency.IdempotencyStore
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CaffeineIdempotencyStore : IdempotencyStore {
    private val cache: Cache<String, Boolean> =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(TTL_MINUTES))
            .maximumSize(MAX_SIZE)
            .build()

    override fun tryLock(key: String): Boolean {
        val existing = cache.asMap().putIfAbsent(key, true)
        return existing == null
    }

    override fun release(key: String) {
        cache.invalidate(key)
    }

    companion object {
        private const val TTL_MINUTES = 1L
        private const val MAX_SIZE = 10_000L
    }
}
