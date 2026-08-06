package com.linktrip.application.port.output.quota

import com.linktrip.application.domain.quota.ApiCallCount
import com.linktrip.application.domain.quota.ApiType
import java.time.LocalDate

/**
 * 일별 API 호출 카운트 영속화 포트.
 * 구현체는 (api_type, call_date) 단일 row 를 유지하며 호출 횟수를 atomic 하게 +1 한다.
 */
interface ApiCallCountPersistencePort {
    fun increment(apiCallCount: ApiCallCount)

    fun findByApiTypeAndDate(
        apiType: ApiType,
        date: LocalDate,
    ): ApiCallCount?

    fun findAllByDate(date: LocalDate): List<ApiCallCount>
}
