package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoAnalysisResultSaver(
    private val travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
) {
    @Transactional
    fun save(
        videoAnalysisTaskId: String,
        itineraryItems: List<TravelItineraryItem>,
    ) {
        travelItineraryItemPersistencePort.saveAll(itineraryItems)
        videoAnalysisTaskPersistencePort.updateValidAndStatus(
            videoAnalysisTaskId,
            valid = true,
            VideoAnalysisTaskStatus.COMPLETED,
        )
    }
}
