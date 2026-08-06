package com.linktrip.application.domain.quota

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 일자별로 마지막으로 발송한 비용 알림 임계값 (KRW).
 * 같은 임계값 구간에 대해 알림이 두 번 나가지 않게 추적하는 용도.
 */
data class ApiDailyCostAlert(
    val id: String,
    val alertDate: LocalDate,
    val lastSentThresholdKrw: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            alertDate: LocalDate,
            lastSentThresholdKrw: Long,
        ): ApiDailyCostAlert =
            ApiDailyCostAlert(
                id = IdGenerator.generate(),
                alertDate = alertDate,
                lastSentThresholdKrw = lastSentThresholdKrw,
            )
    }
}
