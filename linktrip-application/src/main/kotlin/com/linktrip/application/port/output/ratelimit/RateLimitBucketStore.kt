package com.linktrip.application.port.output.ratelimit

interface RateLimitBucketStore {
    /**
     * 주어진 key와 정책으로 토큰 소모를 시도한다.
     *
     * @param key 사용자 식별 키 (예: memberId:POLICY_NAME)
     * @param policy 적용할 rate limit 정책
     * @return true면 허용, false면 rate limit 초과
     */
    fun tryConsume(
        key: String,
        policy: RateLimitPolicy,
    ): Boolean
}
