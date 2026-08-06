package com.linktrip.application.domain.quota

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 특정 [apiType] 의 [callDate] 일자 호출 누적 카운트.
 * UPSERT 의 UPDATE 경로에서는 SQL 측 `call_count + 1` 로 누적되며 [callCount] 값은 사용되지 않는다.
 */
data class ApiCallCount(
    val id: String,
    val apiType: ApiType,
    val callDate: LocalDate,
    val callCount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            apiType: ApiType,
            callDate: LocalDate,
        ): ApiCallCount =
            ApiCallCount(
                id = IdGenerator.generate(),
                apiType = apiType,
                callDate = callDate,
                callCount = 1L,
            )
    }
}
