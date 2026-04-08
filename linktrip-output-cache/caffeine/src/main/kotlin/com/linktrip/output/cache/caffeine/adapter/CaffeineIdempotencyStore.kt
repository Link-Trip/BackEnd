package com.linktrip.output.cache.caffeine.adapter

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.linktrip.application.port.output.idempotency.CachedResponse
import com.linktrip.application.port.output.idempotency.IdempotencyStatus
import com.linktrip.application.port.output.idempotency.IdempotencyStore
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CaffeineIdempotencyStore : IdempotencyStore {
    private val cache: Cache<String, CachedResponse> =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(TTL_MINUTES))
            .maximumSize(MAX_SIZE)
            .build()

    override fun find(key: String): CachedResponse? =
        cache.getIfPresent(key)

    override fun tryLock(key: String): Boolean {
        val processing = CachedResponse(status = IdempotencyStatus.PROCESSING)
        val existing = cache.get(key) { processing }
        return existing === processing
    }

    override fun saveCompleted(key: String, body: Any?) {
        cache.put(key, CachedResponse(status = IdempotencyStatus.COMPLETED, body = body))
    }

    override fun saveFailed(key: String) {
        cache.invalidate(key)
    }

    companion object {
        private const val TTL_MINUTES = 10L
        private const val MAX_SIZE = 10_000L
    }
}
