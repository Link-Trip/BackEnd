package com.linktrip.application.domain.quota

import com.linktrip.application.port.output.quota.ApiCallCountPersistencePort
import com.linktrip.application.port.output.quota.ApiQuotaPolicyPort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

/**
 * 외부 API 일일 호출 한도 가드.
 * 서비스 레이어에서 호출 직전 [isExceeded] 로 체크, 큐 컨슈머는 [isAnyApiExceeded] 로 dequeue 차단.
 * 자정 지나면 [LocalDate.now] 가 바뀌어 자동 해제.
 */
@Service
class ApiQuotaGuardService(
    private val countPort: ApiCallCountPersistencePort,
    private val policyPort: ApiQuotaPolicyPort,
) {
    fun isAnyApiExceeded(): Boolean = ApiType.entries.any { isExceeded(it) }

    fun isExceeded(apiType: ApiType): Boolean {
        val limit = policyPort.dailyLimit(apiType) ?: return false
        val current = countPort.findByApiTypeAndDate(apiType, LocalDate.now())?.callCount ?: 0L
        val exceeded = current >= limit
        if (exceeded) {
            logger.warn { "$apiType 일일 한도 초과: 현재=$current / 한도=$limit" }
        }
        return exceeded
    }
}
