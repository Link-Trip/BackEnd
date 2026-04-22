package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.common.config.event.Events
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VideoAnalyzeService(
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
) : VideoAnalyzeUseCase {
    @Transactional
    override fun analyzeVideo(
        youtubeUrl: String,
        source: Source,
    ): VideoAnalysisTask {
        val normalizedUrl = VideoAnalysisTask.normalizeUrl(youtubeUrl)

        videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)?.let { existing ->
            // FAILED 가 아니면 현재 상태를 그대로 반환 (이미 처리 중이거나 완료된 건 재분석하지 않음).
            if (existing.status != VideoAnalysisTaskStatus.FAILED) return existing

            videoAnalysisTaskPersistencePort.updateStatus(existing.id, VideoAnalysisTaskStatus.PENDING)
            Events.raise(VideoAnalyzeEvent(existing.id, normalizedUrl, source))
            return existing.copy(status = VideoAnalysisTaskStatus.PENDING)
        }

        val videoAnalysisTask =
            videoAnalysisTaskPersistencePort.save(VideoAnalysisTask.create(normalizedUrl, source))
        Events.raise(VideoAnalyzeEvent(videoAnalysisTask.id, normalizedUrl, source))

        return videoAnalysisTask
    }
}
