package com.linktrip.output.http.adapter

import com.google.auth.oauth2.GoogleCredentials
import com.linktrip.application.domain.quota.ApiCallCounterService
import com.linktrip.application.domain.quota.ApiQuotaGuardService
import com.linktrip.application.domain.quota.ApiType
import com.linktrip.application.domain.video.PlaceSearchResult
import com.linktrip.application.port.output.external.GooglePlacesPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.http.properties.GcpProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.io.FileInputStream

@Component
class GooglePlacesAdapter(
    private val gcpProperties: GcpProperties,
    private val googlePlacesRestClient: RestClient,
    private val apiQuotaGuardService: ApiQuotaGuardService,
    private val apiCallCounterService: ApiCallCounterService,
) : GooglePlacesPort {
    private val credentials: GoogleCredentials by lazy {
        GoogleCredentials.fromStream(FileInputStream(gcpProperties.credentialsPath))
            .createScoped("https://www.googleapis.com/auth/cloud-platform")
    }

    override fun searchPlace(
        name: String,
        destination: String?,
    ): PlaceSearchResult? {
        if (apiQuotaGuardService.isExceeded(ApiType.GOOGLE_PLACES)) {
            throw LinktripException(ExceptionCode.BAD_GATEWAY_GOOGLE_PLACES)
        }

        val query = if (destination != null) "$name $destination" else name

        credentials.refreshIfExpired()
        val accessToken = credentials.accessToken.tokenValue

        val response =
            googlePlacesRestClient.post()
                .uri(TEXT_SEARCH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $accessToken")
                .body(
                    mapOf(
                        "textQuery" to query,
                        "languageCode" to "ko",
                    ),
                )
                .retrieve()
                .body<TextSearchResponse>()
        apiCallCounterService.recordSuccess(ApiType.GOOGLE_PLACES)

        val place = response?.places?.firstOrNull() ?: return null

        return PlaceSearchResult(
            googlePlaceId = place.id,
            name = place.displayName?.text ?: name,
            address = place.formattedAddress,
            latitude = place.location?.latitude,
            longitude = place.location?.longitude,
        )
    }

    companion object {
        private const val TEXT_SEARCH_URI = "/places:searchText"
    }

    data class TextSearchResponse(
        val places: List<PlaceDto>?,
    )

    data class PlaceDto(
        val id: String,
        val displayName: DisplayName?,
        val formattedAddress: String?,
        val location: Location?,
    )

    data class DisplayName(
        val text: String?,
        val languageCode: String?,
    )

    data class Location(
        val latitude: Double?,
        val longitude: Double?,
    )
}
