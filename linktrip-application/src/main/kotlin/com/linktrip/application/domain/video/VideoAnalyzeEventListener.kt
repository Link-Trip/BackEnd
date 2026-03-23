package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalyzeEventListener(
    private val videoAnalyzePort: VideoAnalyzePort,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val videoAnalysisResultSaver: VideoAnalysisResultSaver,
    private val placeEnrichService: PlaceEnrichService,
    private val videoAnalysisNotificationPort: VideoAnalysisNotificationPort,
) {
    @Async("VideoAnalyzeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: VideoAnalyzeEvent) {
        val startTime = System.currentTimeMillis()
        logger.info { "영상 분석 시작: id=${event.videoAnalysisTaskId}, url=${event.youtubeUrl}" }

        val destination: String?

        try {
            val result = videoAnalyzePort.analyze(event.youtubeUrl)

            if (!result.valid) {
                logger.warn { "유효하지 않은 영상: id=${event.videoAnalysisTaskId}" }
                videoAnalysisTaskPersistencePort.updateValidAndStatus(
                    event.videoAnalysisTaskId,
                    valid = false,
                    VideoAnalysisTaskStatus.INVALID,
                )
                return
            }

            destination = result.destination
            val itineraryItems = toItineraryItems(event.videoAnalysisTaskId, result)
            videoAnalysisResultSaver.save(event.videoAnalysisTaskId, itineraryItems)

            val analyzeElapsed = System.currentTimeMillis() - startTime
            logger.info {
                "영상 분석 완료: id=${event.videoAnalysisTaskId}, destination=$destination, " +
                    "${analyzeElapsed}ms, items=${itineraryItems.size}"
            }
        } catch (e: Exception) {
            logger.error(e) { "영상 분석 실패: id=${event.videoAnalysisTaskId}" }
            videoAnalysisTaskPersistencePort.updateStatus(event.videoAnalysisTaskId, VideoAnalysisTaskStatus.FAILED)
            return
        }

        enrichPlaces(event.videoAnalysisTaskId, destination)
        videoAnalysisNotificationPort.notifyAnalysisComplete(event.videoAnalysisTaskId)
    }

    private fun enrichPlaces(
        videoAnalysisTaskId: String,
        destination: String?,
    ) {
        try {
            val startTime = System.currentTimeMillis()
            placeEnrichService.enrichPlaces(videoAnalysisTaskId, destination)
            val elapsed = System.currentTimeMillis() - startTime
            logger.info { "장소 보강 소요시간: id=$videoAnalysisTaskId, ${elapsed}ms" }
        } catch (e: Exception) {
            logger.warn(e) { "장소 보강 실패, 분석 결과는 유지: id=$videoAnalysisTaskId" }
        }
    }

    private fun toItineraryItems(
        videoAnalysisTaskId: String,
        result: VideoAnalysisResult,
    ): List<TravelItineraryItem> =
        result.days.flatMap { daySchedule ->
            daySchedule.items.map { item ->
                TravelItineraryItem.from(videoAnalysisTaskId, daySchedule, item)
            }
        }
}
