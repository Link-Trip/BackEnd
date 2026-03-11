package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
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
    private val videoAnalysisResultSaver: VideoAnalysisResultSaver,
    private val placeEnrichService: PlaceEnrichService,
    private val videoAnalysisNotificationPort: VideoAnalysisNotificationPort,
) {
    @Async("VideoAnalyzeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoAnalyzeEvent) {
        val startTime = System.currentTimeMillis()
        logger.info { "영상 분석 시작: id=${event.videoSummaryId}, url=${event.youtubeUrl}" }

        try {
            val result = videoAnalyzePort.analyze(event.youtubeUrl)

            if (!result.valid) {
                logger.warn { "유효하지 않은 영상: id=${event.videoSummaryId}" }
                videoSummaryPersistencePort.updateValidAndStatus(
                    event.videoSummaryId,
                    valid = false,
                    VideoSummaryStatus.INVALID,
                )
                return
            }

            val destination: String? = result.destination
            val scheduleItems = toScheduleItems(event.videoSummaryId, result)
            videoAnalysisResultSaver.save(event.videoSummaryId, scheduleItems)

            val analyzeElapsed = System.currentTimeMillis() - startTime
            logger.info {
                "영상 분석 완료: id=${event.videoSummaryId}, destination=$destination, " +
                    "${analyzeElapsed}ms, items=${scheduleItems.size}"
            }

            val enrichStartTime = System.currentTimeMillis()
            placeEnrichService.enrichPlaces(event.videoSummaryId, destination)
            val enrichElapsed = System.currentTimeMillis() - enrichStartTime
            logger.info { "장소 보강 소요시간: id=${event.videoSummaryId}, ${enrichElapsed}ms" }

            videoAnalysisNotificationPort.notifyAnalysisComplete(event.videoSummaryId)
        } catch (e: Exception) {
            logger.error(e) { "영상 분석 실패: id=${event.videoSummaryId}" }
            videoSummaryPersistencePort.updateStatus(event.videoSummaryId, VideoSummaryStatus.FAILED)
        }
    }

    private fun toScheduleItems(
        videoSummaryId: String,
        result: VideoAnalysisResult,
    ): List<VideoScheduleItem> =
        result.days.flatMap { daySchedule ->
            daySchedule.items.map { item ->
                VideoScheduleItem.from(videoSummaryId, daySchedule, item)
            }
        }
}
