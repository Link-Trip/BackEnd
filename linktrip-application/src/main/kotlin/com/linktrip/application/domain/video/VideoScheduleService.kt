package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoScheduleResult
import com.linktrip.application.port.input.VideoScheduleUseCase
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VideoScheduleService(
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort,
) : VideoScheduleUseCase {
    override fun getVideoSchedule(videoAnalysisTaskId: String): VideoScheduleResult {
        val videoAnalysisTask =
            videoAnalysisTaskPersistencePort.findById(videoAnalysisTaskId)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND)

        val items = travelItineraryItemPersistencePort.findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId)

        return VideoScheduleResult(videoAnalysisTask, items)
    }
}
