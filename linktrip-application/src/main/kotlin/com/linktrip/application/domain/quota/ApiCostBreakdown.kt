package com.linktrip.application.domain.quota

/**
 * 특정 일자의 API 별 누적 비용 (KRW) 분해.
 * 합계 / 0원 제외 필터 등 표현/판정 로직을 도메인에 포함.
 */
data class ApiCostBreakdown(
    val items: List<ApiCostItem>,
) {
    val totalKrw: Long = items.sumOf { it.costKrw }

    /** 비용 0원인 API 는 제외 (Discord 메시지에 노출할 항목만). */
    fun nonZero(): List<ApiCostItem> = items.filter { it.costKrw > 0L }
}

data class ApiCostItem(
    val apiType: ApiType,
    val costKrw: Long,
)
