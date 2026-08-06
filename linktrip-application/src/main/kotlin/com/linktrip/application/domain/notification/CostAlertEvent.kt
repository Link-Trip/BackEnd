package com.linktrip.application.domain.notification

import com.linktrip.application.domain.quota.ApiCostBreakdown
import java.time.LocalDate

/**
 * 외부 API 누적 비용이 임계값(1000원 단위)을 새로 넘었을 때 발송되는 알림 이벤트.
 */
data class CostAlertEvent(
    val date: LocalDate,
    val thresholdKrw: Long,
    val breakdown: ApiCostBreakdown,
)
