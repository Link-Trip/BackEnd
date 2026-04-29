package com.linktrip.application.port.output.quota

import com.linktrip.application.domain.quota.ApiType

/**
 * API 별 일일 호출 한도 정책 (yml 등 외부 소스에서 제공).
 * null = 한도 미설정 = 가드 비활성.
 */
interface ApiQuotaPolicyPort {
    fun dailyLimit(apiType: ApiType): Long?
}
