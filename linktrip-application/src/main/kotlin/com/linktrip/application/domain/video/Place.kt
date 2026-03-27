package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class Place(
    val id: String,
    val name: String,
    val googlePlaceId: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(result: PlaceSearchResult): Place =
            Place(
                id = IdGenerator.generate(),
                name = result.name,
                googlePlaceId = result.googlePlaceId,
                address = result.address,
                latitude = result.latitude,
                longitude = result.longitude,
            )
    }
}
