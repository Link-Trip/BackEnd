package com.linktrip.application.port.output.idempotency

enum class IdempotencyStatus {
    /** 요청 처리 중 (따닥 방지용 락) */
    PROCESSING,

    /** 요청 처리 완료 (이전 결과 캐싱됨) */
    COMPLETED,

    /** 요청 처리 실패 (같은 키로 재시도 허용) */
    FAILED,
}
