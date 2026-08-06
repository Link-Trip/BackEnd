package com.linktrip.application.domain.quota

import com.linktrip.application.domain.notification.CostAlertEvent
import com.linktrip.application.port.output.notification.NotificationPort
import com.linktrip.application.port.output.quota.ApiCallCountPersistencePort
import com.linktrip.application.port.output.quota.ApiCostPolicyPort
import com.linktrip.application.port.output.quota.ApiDailyCostAlertPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

/**
 * 외부 API 호출 성공 시의 부수 처리 (카운트 적재 + 누적 비용 임계값 알림) 를 담당한다.
 *
 * 서비스 레이어에서 호출 직후 한 줄로 호출:
 * ```
 * val r = port.callApi(...)
 * apiCallCounterService.recordSuccess(ApiType.X)
 * ```
 *
 * 실패 처리 정책:
 * - **카운트 실패는 그대로 throw**. 카운트가 실제 호출 수와 어긋나면 가드가 한도 초과를 감지 못 해 비용 폭주 위험.
 *   본 PR 의 핵심 가치 (비용 보호) 를 지키려면 fail-loud 가 맞다.
 * - **알림 실패는 로그만**. 알림이 늦어져도 가드는 정상 작동하므로 비용 보호에는 영향 없음.
 */
@Service
class ApiCallCounterService(
    private val countPort: ApiCallCountPersistencePort,
    private val costPolicyPort: ApiCostPolicyPort,
    private val alertPort: ApiDailyCostAlertPersistencePort,
    private val notificationPort: NotificationPort,
) {
    fun recordSuccess(apiType: ApiType) {
        countPort.increment(ApiCallCount.create(apiType, LocalDate.now()))

        try {
            notifyIfCostThresholdCrossed()
        } catch (e: Exception) {
            logger.warn(e) { "비용 알림 체크 실패: $apiType" }
        }
    }

    private fun notifyIfCostThresholdCrossed() {
        val today = LocalDate.now()
        val breakdown = computeCostBreakdown(today)
        val lastSent = alertPort.findLastSentThresholdKrw(today) ?: 0L

        if (breakdown.totalKrw < lastSent + THRESHOLD_KRW) return

        val newThreshold = (breakdown.totalKrw / THRESHOLD_KRW) * THRESHOLD_KRW
        notificationPort.sendCostAlert(
            CostAlertEvent(
                date = today,
                thresholdKrw = newThreshold,
                breakdown = breakdown,
            ),
        )
        alertPort.upsert(ApiDailyCostAlert.create(today, newThreshold))
        logger.info { "비용 임계값 알림 발송: total=${breakdown.totalKrw}원, threshold=${newThreshold}원" }
    }

    private fun computeCostBreakdown(date: LocalDate): ApiCostBreakdown {
        val countByApi = countPort.findAllByDate(date).associateBy { it.apiType }
        val items =
            ApiType.entries.map { apiType ->
                val count = countByApi[apiType]?.callCount ?: 0L
                ApiCostItem(apiType, count * costPolicyPort.perCallKrw(apiType))
            }
        return ApiCostBreakdown(items)
    }

    companion object {
        /** 알림 임계값 단위 (KRW). 1000원마다 한 번 발송. */
        private const val THRESHOLD_KRW = 1000L
    }
}
