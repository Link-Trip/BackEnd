package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 외부 API 일일 호출 한도 (yml 의 `api.daily-quota.*`).
 * null 또는 미설정 = 한도 무시.
 */
@ConfigurationProperties(prefix = "api.daily-quota")
data class ApiQuotaProperties(
    val gemini: Long? = null,
    val youtubeData: Long? = null,
    val googlePlaces: Long? = null,
)
