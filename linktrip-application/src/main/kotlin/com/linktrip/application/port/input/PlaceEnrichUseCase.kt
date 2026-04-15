package com.linktrip.application.port.input

interface PlaceEnrichUseCase {
    fun enrichPlaces(
        videoAnalysisTaskId: String,
        destination: String? = null,
    )

    fun retryAll()
}
