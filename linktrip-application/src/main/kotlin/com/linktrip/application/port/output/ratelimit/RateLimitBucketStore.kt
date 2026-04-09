package com.linktrip.application.port.output.ratelimit

interface RateLimitBucketStore {
    /**
     * 즉시 토큰 소모를 시도한다. 한도 초과 시 false를 반환하고 요청을 거부한다.
     * 용도: API Rate Limiting (사용자 요청 제한)
     */
    fun tryConsumeOrReject(
        key: String,
        policy: RateLimitPolicy,
    ): Boolean

    /**
     * 토큰이 사용 가능할 때까지 대기한 후 소모한다. 한도 초과 시 여유가 생길 때까지 스레드를 차단한다.
     * 용도: 큐 소비자처럼 순서대로 처리해야 하는 경우
     */
    fun waitAndConsume(
        key: String,
        policy: RateLimitPolicy,
    )
}
