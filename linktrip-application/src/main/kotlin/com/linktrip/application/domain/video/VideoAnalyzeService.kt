package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.common.config.event.Events
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VideoAnalyzeService(
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
) : VideoAnalyzeUseCase {
    @Transactional
    override fun analyzeVideo(youtubeUrl: String): VideoAnalysisTask {
        validateYoutubeUrl(youtubeUrl)

        videoAnalysisTaskPersistencePort.findByYoutubeUrl(youtubeUrl)?.let { existing ->
            return when (existing.status) {
                VideoAnalysisTaskStatus.PENDING -> existing
                VideoAnalysisTaskStatus.COMPLETED -> existing
                VideoAnalysisTaskStatus.INVALID -> existing
                VideoAnalysisTaskStatus.FAILED -> {
                    videoAnalysisTaskPersistencePort.updateStatus(existing.id, VideoAnalysisTaskStatus.PENDING)
                    Events.raise(VideoAnalyzeEvent(existing.id, youtubeUrl))
                    existing.copy(status = VideoAnalysisTaskStatus.PENDING)
                }
            }
        }

        val videoAnalysisTask = videoAnalysisTaskPersistencePort.save(VideoAnalysisTask.create(youtubeUrl))

        Events.raise(VideoAnalyzeEvent(videoAnalysisTask.id, youtubeUrl))

        return videoAnalysisTask
    }

    private fun validateYoutubeUrl(url: String) {
        if (!YOUTUBE_URL_REGEX.matches(url)) {
            throw LinktripException(ExceptionCode.INVALID_YOUTUBE_URL)
        }
    }

    companion object {
        private val YOUTUBE_URL_REGEX =
            Regex("^https?://(www\\.|m\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]+.*$")
    }
}
