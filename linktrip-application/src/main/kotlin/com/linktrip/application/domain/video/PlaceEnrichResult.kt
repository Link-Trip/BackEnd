package com.linktrip.application.domain.video

data class PlaceEnrichResult(
    val itemId: String,
    val place: PlaceSearchResult?,
    val success: Boolean,
)
