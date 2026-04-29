package com.linktrip.application.port.output.quota

import com.linktrip.application.domain.quota.ApiType

/**
 * API 별 호출 1회당 추정 비용 (KRW). yml 등 외부 소스에서 제공.
 * 0 또는 null = 비용 추적 비활성.
 */
interface ApiCostPolicyPort {
    fun perCallKrw(apiType: ApiType): Long
}
