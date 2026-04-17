package com.linktrip.input.batch

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 수집은 됐지만 (youtube_video) 한 번도 분석 요청이 안 된 영상 (= video_analysis_task 미존재)
 * 을 BATCH 우선순위로 큐에 적재해 자동 분석시키는 스케줄러.
 *
 * **활성화**: `batch.video-analysis-backfill.enabled=true` 인 환경에서만 빈 등록.
 * dev 등 토큰 비용 통제가 필요한 환경에서는 자동 실행되지 않는다.
 *
 * **우선순위**: 사용자 USER 요청은 큐에서 항상 먼저 처리되므로 이 자동 분석이 사용자 응답을 지연시키지 않는다.
 */
@Component
@ConditionalOnProperty(name = ["batch.video-analysis-backfill.enabled"], havingValue = "true")
class VideoAnalysisBackfillScheduler(
    private val youTubeVideoPersistencePort: YouTubeVideoPersistencePort,
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
) {
    @Scheduled(cron = SCHEDULE_CRON)
    fun run() {
        val videoIds = youTubeVideoPersistencePort.findUnanalyzedVideoIds(BATCH_SIZE)
        if (videoIds.isEmpty()) {
            logger.info { "미처리 영상 없음" }
            return
        }

        logger.info { "수집 영상 자동 분석 큐 적재 시작: ${videoIds.size}건 (오래된 순)" }
        var success = 0
        videoIds.forEach { videoId ->
            try {
                videoAnalyzeUseCase.analyzeVideo(
                    youtubeUrl = VideoAnalysisTask.buildUrl(videoId),
                    source = Source.BATCH,
                )
                success++
            } catch (e: Exception) {
                logger.warn(e) { "수집 영상 자동 분석 요청 실패: videoId=$videoId" }
            }
        }
        logger.info { "수집 영상 자동 분석 큐 적재 완료: 성공=$success, 실패=${videoIds.size - success}" }
    }

    companion object {
        /** 10분마다 실행. 사용자 트래픽 대비 적당한 페이스. */
        private const val SCHEDULE_CRON = "0 */10 * * * *"

        /** 1회 실행당 enqueue 건수. USER 요청이 끼어들 여지를 위해 작게. */
        private const val BATCH_SIZE = 5
    }
}
