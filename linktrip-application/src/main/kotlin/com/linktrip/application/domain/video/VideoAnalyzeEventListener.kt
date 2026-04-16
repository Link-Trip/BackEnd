package com.linktrip.application.domain.video

import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalyzeEventListener(
    private val videoAnalysisQueuePort: VideoAnalysisQueuePort,
) {
    @Async("VideoAnalyzeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoAnalyzeEvent) {
        logger.info { "영상 분석 큐 등록: id=${event.videoAnalysisTaskId}, url=${event.youtubeUrl}" }
        videoAnalysisQueuePort.enqueue(event.videoAnalysisTaskId, event.youtubeUrl)
    }
}
