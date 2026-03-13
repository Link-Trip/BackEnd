package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoScheduleResult
import com.linktrip.application.port.input.VideoScheduleUseCase
import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VideoScheduleService(
    private val videoSummaryPersistencePort: VideoSummaryPersistencePort,
    private val videoScheduleItemPersistencePort: VideoScheduleItemPersistencePort,
) : VideoScheduleUseCase {
    override fun getVideoSchedule(videoSummaryId: String): VideoScheduleResult {
        val videoSummary =
            videoSummaryPersistencePort.findById(videoSummaryId)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND)

        val items = videoScheduleItemPersistencePort.findByVideoSummaryIdWithPlace(videoSummaryId)

        return VideoScheduleResult(videoSummary, items)
    }
}
