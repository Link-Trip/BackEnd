package com.linktrip.output.http.adapter

import com.linktrip.application.domain.quota.ApiType
import com.linktrip.application.port.output.quota.ApiQuotaPolicyPort
import com.linktrip.output.http.properties.ApiQuotaProperties
import org.springframework.stereotype.Component

@Component
class PropertiesApiQuotaPolicyAdapter(
    private val properties: ApiQuotaProperties,
) : ApiQuotaPolicyPort {
    override fun dailyLimit(apiType: ApiType): Long? =
        when (apiType) {
            ApiType.GEMINI -> properties.gemini
            ApiType.YOUTUBE_DATA -> properties.youtubeData
            ApiType.GOOGLE_PLACES -> properties.googlePlaces
        }
}
