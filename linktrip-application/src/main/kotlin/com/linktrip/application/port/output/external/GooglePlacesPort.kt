package com.linktrip.application.port.output.external

import com.linktrip.application.domain.video.PlaceSearchResult

interface GooglePlacesPort {
    fun searchPlace(
        name: String,
        destination: String? = null,
    ): PlaceSearchResult?
}
