package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoAnalysisResultSaver(
    private val videoScheduleItemPersistencePort: VideoScheduleItemPersistencePort,
    private val videoSummaryPersistencePort: VideoSummaryPersistencePort,
) {
    @Transactional
    fun save(
        videoSummaryId: String,
        scheduleItems: List<VideoScheduleItem>,
    ) {
        videoScheduleItemPersistencePort.saveAll(scheduleItems)
        videoSummaryPersistencePort.updateValidAndStatus(
            videoSummaryId,
            valid = true,
            VideoSummaryStatus.COMPLETED,
        )
    }
}
