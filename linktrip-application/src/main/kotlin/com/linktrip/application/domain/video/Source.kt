package com.linktrip.application.domain.video

/**
 * 영상 분석 요청의 출처. 큐 우선순위 결정에 사용된다.
 *
 * priority 가 작을수록 먼저 dequeue 된다 (Java [java.util.PriorityQueue] 컨벤션).
 */
enum class Source(val priority: Int) {
    /** 사용자 직접 요청 (POST /video/analyze 등). 항상 먼저 처리. */
    USER(0),

    /** 시스템 배치/수집 (YouTube 정기 수집, stranded backfill  등). USER 가 비었을 때만 처리. */
    BATCH(10),
}
