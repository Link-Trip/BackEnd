package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalyzeEventListener(
    private val videoAnalyzePort: VideoAnalyzePort,
    private val videoSummaryPersistencePort: VideoSummaryPersistencePort,
) {
    @Async("VideoAnalyzeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoAnalyzeEvent) {
        try {
            val result = videoAnalyzePort.analyze(event.youtubeUrl)
            if (result.valid) {
                videoSummaryPersistencePort.updateSummaryAndStatus(
                    event.videoSummaryId,
                    result,
                    VideoSummaryStatus.COMPLETED,
                )
            } else {
                videoSummaryPersistencePort.updateStatus(
                    event.videoSummaryId,
                    VideoSummaryStatus.FAILED,
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "비디오 분석 실패: id=${event.videoSummaryId}, url=${event.youtubeUrl}" }
            videoSummaryPersistencePort.updateStatus(
                event.videoSummaryId,
                VideoSummaryStatus.FAILED,
            )
        }
    }
}
