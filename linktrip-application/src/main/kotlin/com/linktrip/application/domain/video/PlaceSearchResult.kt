package com.linktrip.application.domain.video

data class PlaceSearchResult(
    val googlePlaceId: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
)
