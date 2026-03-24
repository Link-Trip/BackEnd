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
    override fun analyzeVideo(youtubeUrl: String): VideoAnalysisTask {
        val normalizedUrl = VideoAnalysisTask.normalizeUrl(youtubeUrl)

        videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)?.let { existing ->
            return when (existing.status) {
                VideoAnalysisTaskStatus.PENDING -> existing
                VideoAnalysisTaskStatus.COMPLETED -> existing
                VideoAnalysisTaskStatus.INVALID -> existing
                VideoAnalysisTaskStatus.FAILED -> {
                    videoAnalysisTaskPersistencePort.updateStatus(existing.id, VideoAnalysisTaskStatus.PENDING)
                    Events.raise(VideoAnalyzeEvent(existing.id, normalizedUrl))
                    existing.copy(status = VideoAnalysisTaskStatus.PENDING)
                }
            }
        }

        val videoAnalysisTask = videoAnalysisTaskPersistencePort.save(VideoAnalysisTask.create(normalizedUrl))

        Events.raise(VideoAnalyzeEvent(videoAnalysisTask.id, normalizedUrl))

        return videoAnalysisTask
    }
}
