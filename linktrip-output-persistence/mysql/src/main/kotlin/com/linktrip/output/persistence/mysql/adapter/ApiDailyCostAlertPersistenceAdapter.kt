package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.quota.ApiDailyCostAlert
import com.linktrip.application.port.output.quota.ApiDailyCostAlertPersistencePort
import com.linktrip.output.persistence.mysql.entity.ApiDailyCostAlertEntity
import com.linktrip.output.persistence.mysql.repository.ApiDailyCostAlertJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 일자별 비용 알림 임계값 추적 어댑터.
 *
 * 알림 임계값 통과 순간에만 호출되는 저빈도 경로라 단순한 SELECT → 변경/INSERT 흐름으로 처리.
 * - row 가 있으면 dirty checking 으로 lastSentThresholdKrw 갱신.
 * - row 가 없으면 신규 INSERT.
 */
@Component
class ApiDailyCostAlertPersistenceAdapter(
    private val jpaRepository: ApiDailyCostAlertJpaRepository,
) : ApiDailyCostAlertPersistencePort {
    @Transactional(readOnly = true)
    override fun findLastSentThresholdKrw(date: LocalDate): Long? = jpaRepository.findByAlertDate(date)?.lastSentThresholdKrw

    @Transactional
    override fun upsert(alert: ApiDailyCostAlert) {
        val existing = jpaRepository.findByAlertDate(alert.alertDate)
        if (existing != null) {
            existing.lastSentThresholdKrw = alert.lastSentThresholdKrw
        } else {
            jpaRepository.save(ApiDailyCostAlertEntity.from(alert))
        }
    }
}
