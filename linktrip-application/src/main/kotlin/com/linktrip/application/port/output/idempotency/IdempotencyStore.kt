package com.linktrip.application.port.output.idempotency

/**
 * 처리 중인 요청의 중복 실행을 막기 위한 락 저장소.
 *
 * 로컬 환경에서는 Caffeine, 분산 환경에서는 Redis 등으로 구현체를 교체한다.
 *
 * @see com.linktrip.application.port.output.ratelimit.RateLimitBucketStore 동일 패턴
 */
interface IdempotencyStore {
    /**
     * 키에 대해 처리 중 락을 건다.
     * 이미 처리 중인 동일 키가 존재하면 false를 반환한다.
     */
    fun tryLock(key: String): Boolean

    /**
     * 처리 완료 또는 실패 후 락을 해제한다.
     */
    fun release(key: String)
}
