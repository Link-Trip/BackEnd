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
        logger.info { "비디오 분석 시작: id=${event.videoSummaryId}, url=${event.youtubeUrl}" }
        val startTime = System.currentTimeMillis()
        try {
            val result = videoAnalyzePort.analyze(event.youtubeUrl)
            val elapsed = System.currentTimeMillis() - startTime
            logger.info { "비디오 분석 완료: id=${event.videoSummaryId}, valid=${result.valid}, 소요시간=${elapsed}ms" }
            logger.debug { "비디오 분석 결과: id=${event.videoSummaryId}, result=$result" }
            if (result.valid) {
                videoSummaryPersistencePort.updateSummaryAndStatus(
                    event.videoSummaryId,
                    result,
                    VideoSummaryStatus.COMPLETED,
                )
            } else {
                logger.warn { "비디오 분석 결과 유효하지 않음: id=${event.videoSummaryId}" }
                videoSummaryPersistencePort.updateStatus(
                    event.videoSummaryId,
                    VideoSummaryStatus.FAILED,
                )
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            logger.error(e) { "비디오 분석 실패: id=${event.videoSummaryId}, url=${event.youtubeUrl}, 소요시간=${elapsed}ms" }
            videoSummaryPersistencePort.updateStatus(
                event.videoSummaryId,
                VideoSummaryStatus.FAILED,
            )
        }
    }
}
