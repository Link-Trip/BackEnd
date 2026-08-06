package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * API 1회 호출당 추정 비용 (KRW). yml 의 `api.cost-per-call-krw.*`.
 * 0 = 비용 추적 비활성.
 */
@ConfigurationProperties(prefix = "api.cost-per-call-krw")
data class ApiCostProperties(
    val gemini: Long = 0L,
    val youtubeData: Long = 0L,
    val googlePlaces: Long = 0L,
)
