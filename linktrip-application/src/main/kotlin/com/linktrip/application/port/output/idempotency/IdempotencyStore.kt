package com.linktrip.application.port.output.idempotency

/**
 * 멱등성 키의 상태를 저장하고 조회하는 Output Port.
 *
 * 로컬 환경에서는 Caffeine, 분산 환경에서는 Redis 등으로 구현체를 교체한다.
 *
 * @see com.linktrip.application.port.output.ratelimit.RateLimitBucketStore 동일 패턴
 */
interface IdempotencyStore {
    /**
     * 키에 해당하는 캐시된 응답을 조회한다.
     * 존재하지 않으면 null을 반환한다.
     */
    fun find(key: String): CachedResponse?

    /**
     * ���에 대해 PROCESSING 상태로 락을 건다.
     * 이미 다른 상태가 존재하면 false를 반환한다.
     */
    fun tryLock(key: String): Boolean

    /**
     * 처리 완료된 결과를 저장한다.
     */
    fun saveCompleted(key: String, body: Any?)

    /**
     * 처리 실패를 기록한다.
     * 같은 키로 재시도가 가능하도록 캐시를 제거한다.
     */
    fun saveFailed(key: String)
}
