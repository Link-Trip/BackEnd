package com.linktrip.output.http.adapter

import com.linktrip.application.domain.quota.ApiType
import com.linktrip.application.port.output.quota.ApiCostPolicyPort
import com.linktrip.output.http.properties.ApiCostProperties
import org.springframework.stereotype.Component

@Component
class PropertiesApiCostPolicyAdapter(
    private val properties: ApiCostProperties,
) : ApiCostPolicyPort {
    override fun perCallKrw(apiType: ApiType): Long =
        when (apiType) {
            ApiType.GEMINI -> properties.gemini
            ApiType.YOUTUBE_DATA -> properties.youtubeData
            ApiType.GOOGLE_PLACES -> properties.googlePlaces
        }
}
