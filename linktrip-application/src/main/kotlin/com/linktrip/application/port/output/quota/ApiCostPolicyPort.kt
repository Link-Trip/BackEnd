package com.linktrip.application.port.output.quota

import com.linktrip.application.domain.quota.ApiType

/**
 * API 별 호출 1회당 추정 비용 (KRW). yml 등 외부 소스에서 제공.
 * 0 = 비용 추적 비활성 (구현체는 항상 non-null Long 을 반환).
 */
interface ApiCostPolicyPort {
    fun perCallKrw(apiType: ApiType): Long
}
