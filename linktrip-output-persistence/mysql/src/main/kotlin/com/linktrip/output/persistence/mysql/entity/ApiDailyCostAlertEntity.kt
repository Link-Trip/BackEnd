package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.quota.ApiDailyCostAlert
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

/**
 * 일자별로 마지막 발송된 비용 알림의 임계값 (KRW) 추적.
 * 같은 임계값 구간에서 알림 1회만 발송되도록 함.
 */
@Entity
@Table(
    name = "api_daily_cost_alert",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_api_daily_cost_alert_date", columnNames = ["alert_date"]),
    ],
)
class ApiDailyCostAlertEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "alert_date", nullable = false)
    val alertDate: LocalDate,
    @Column(name = "last_sent_threshold_krw", nullable = false)
    var lastSentThresholdKrw: Long,
) : BaseTimeEntity() {
    fun toDomain(): ApiDailyCostAlert =
        ApiDailyCostAlert(
            id = this.id,
            alertDate = this.alertDate,
            lastSentThresholdKrw = this.lastSentThresholdKrw,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(alert: ApiDailyCostAlert): ApiDailyCostAlertEntity =
            ApiDailyCostAlertEntity(
                id = alert.id,
                alertDate = alert.alertDate,
                lastSentThresholdKrw = alert.lastSentThresholdKrw,
            )
    }
}
