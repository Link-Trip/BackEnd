package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.PlaceEnrichResult

interface PlaceEnrichPersistencePort {
    fun applyResults(
        videoSummaryId: String,
        results: List<PlaceEnrichResult>,
    )
}
