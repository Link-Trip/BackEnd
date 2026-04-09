package com.linktrip.application.domain.video

import com.linktrip.application.domain.trip.TripPlanService
import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import com.linktrip.application.port.output.ratelimit.RateLimitBucketStore
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.application.port.output.ratelimit.RateLimitPolicy
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalysisQueueConsumer(
    private val videoAnalysisQueuePort: VideoAnalysisQueuePort,
    private val videoAnalyzePort: VideoAnalyzePort,
    private val videoAnalysisResultSaver: VideoAnalysisResultSaver,
    private val placeEnrichService: PlaceEnrichService,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val videoAnalysisNotificationPort: VideoAnalysisNotificationPort,
    private val tripPlanRequestPort: TripPlanRequestPersistencePort,
    private val tripPlanService: TripPlanService,
    private val rateLimitBucketStore: RateLimitBucketStore,
) {
    fun startConsuming() {
        Thread({ consumeLoop() }, "VideoAnalysisQueueConsumer").apply {
            isDaemon = true
            start()
        }
        logger.info { "영상 분석 큐 소비자 시작" }
    }

    private fun consumeLoop() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                val event = videoAnalysisQueuePort.dequeue() ?: continue
                rateLimitBucketStore.waitAndConsume(RATE_LIMIT_KEY, RateLimitPolicy.GEMINI_API)
                processAnalysis(event)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.info { "영상 분석 큐 소비자 중단" }
                break
            } catch (e: Exception) {
                logger.error(e) { "영상 분석 큐 소비자 오류" }
            }
        }
    }

    private fun processAnalysis(event: VideoAnalyzeEvent) {
        val startTime = System.currentTimeMillis()
        logger.info { "영상 분석 시작: id=${event.videoAnalysisTaskId}, url=${event.youtubeUrl}" }

        val destination: String?
        val title: String?

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
            title = result.title
            val itineraryItems = toItineraryItems(event.videoAnalysisTaskId, result)
            val timelines = toTimelines(event.videoAnalysisTaskId, result)
            videoAnalysisResultSaver.save(
                event.videoAnalysisTaskId,
                itineraryItems,
                summary = result.summary,
                estimatedMinCost = result.estimatedMinCost,
                estimatedMaxCost = result.estimatedMaxCost,
                costBasis = result.costBasis,
                hashtags = result.hashtags,
                timelines = timelines,
                destination = result.destination,
            )

            val elapsed = System.currentTimeMillis() - startTime
            logger.info {
                "영상 분석 완료: id=${event.videoAnalysisTaskId}, destination=$destination, " +
                    "${elapsed}ms, items=${itineraryItems.size}"
            }
        } catch (e: LinktripException) {
            when (e.exceptionCode) {
                ExceptionCode.BAD_GATEWAY_YOUTUBE -> {
                    logger.warn { "YouTube Rate Limit으로 재시도 큐 등록: id=${event.videoAnalysisTaskId}" }
                    videoAnalysisQueuePort.enqueue(event.videoAnalysisTaskId, event.youtubeUrl)
                }
                ExceptionCode.BAD_REQUEST_VIDEO -> {
                    logger.warn { "자막 없는 영상 INVALID 처리: id=${event.videoAnalysisTaskId}" }
                    videoAnalysisTaskPersistencePort.updateValidAndStatus(
                        event.videoAnalysisTaskId,
                        valid = false,
                        VideoAnalysisTaskStatus.INVALID,
                    )
                }
                else -> {
                    logger.error(e) { "영상 분석 실패: id=${event.videoAnalysisTaskId}" }
                    videoAnalysisTaskPersistencePort.updateStatus(
                        event.videoAnalysisTaskId,
                        VideoAnalysisTaskStatus.FAILED,
                    )
                }
            }
            return
        } catch (e: Exception) {
            logger.error(e) { "영상 분석 실패: id=${event.videoAnalysisTaskId}" }
            videoAnalysisTaskPersistencePort.updateStatus(
                event.videoAnalysisTaskId,
                VideoAnalysisTaskStatus.FAILED,
            )
            return
        }

        processPendingRequests(event.videoAnalysisTaskId, title ?: destination)
        enrichPlaces(event.videoAnalysisTaskId, destination)

        val memberIds = tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId(event.videoAnalysisTaskId)
        videoAnalysisNotificationPort.notifyAnalysisComplete(event.videoAnalysisTaskId, memberIds)
    }

    private fun processPendingRequests(
        videoAnalysisTaskId: String,
        destination: String?,
    ) {
        val requests = tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId)
        if (requests.isEmpty()) return

        val planTitle = destination ?: "여행 계획"
        requests.forEach { request ->
            runCatching {
                tripPlanService.createFromAnalysisIfAbsent(request.memberId, videoAnalysisTaskId, planTitle)
                request.markProcessed()
            }.onFailure { e ->
                logger.error(e) {
                    "여행 계획 자동 생성 실패: taskId=$videoAnalysisTaskId, requestId=${request.id}"
                }
            }
        }
        tripPlanRequestPort.saveAll(requests)

        logger.info {
            "여행 계획 자동 생성: taskId=$videoAnalysisTaskId, " +
                "성공=${requests.count { it.processed }}/${requests.size}"
        }
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

    private fun toTimelines(
        videoAnalysisTaskId: String,
        result: VideoAnalysisResult,
    ): List<VideoTimeline> =
        result.timeline.map { timelineItem ->
            VideoTimeline.create(
                videoAnalysisTaskId = videoAnalysisTaskId,
                timestampSeconds = timelineItem.timestampSeconds,
                description = timelineItem.description,
            )
        }

    companion object {
        private const val RATE_LIMIT_KEY = "gemini-api"
    }
}
