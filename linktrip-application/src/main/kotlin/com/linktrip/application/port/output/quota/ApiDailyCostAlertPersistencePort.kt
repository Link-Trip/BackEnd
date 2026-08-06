package com.linktrip.application.port.output.quota

import com.linktrip.application.domain.quota.ApiDailyCostAlert
import java.time.LocalDate

/**
 * 일자별로 마지막으로 알림 발송한 누적 비용 임계값을 추적한다.
 * 같은 임계값(예: 5000원) 에 대해 알림이 한 번만 발송되도록 함.
 */
interface ApiDailyCostAlertPersistencePort {
    fun findLastSentThresholdKrw(date: LocalDate): Long?

    fun upsert(alert: ApiDailyCostAlert)
}
