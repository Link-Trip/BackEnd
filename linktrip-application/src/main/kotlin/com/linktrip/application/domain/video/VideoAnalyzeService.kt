package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import com.linktrip.common.config.event.Events
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VideoAnalyzeService(
    private val videoSummaryPersistencePort: VideoSummaryPersistencePort,
) : VideoAnalyzeUseCase {
    @Transactional
    override fun analyzeVideo(youtubeUrl: String): VideoSummary {
        validateYoutubeUrl(youtubeUrl)

        videoSummaryPersistencePort.findByYoutubeUrl(youtubeUrl)?.let { existing ->
            return when (existing.status) {
                VideoSummaryStatus.PENDING -> existing
                VideoSummaryStatus.COMPLETED -> existing
                VideoSummaryStatus.FAILED -> {
                    videoSummaryPersistencePort.updateStatus(existing.id, VideoSummaryStatus.PENDING)
                    Events.raise(VideoAnalyzeEvent(existing.id, youtubeUrl))
                    existing.copy(status = VideoSummaryStatus.PENDING)
                }
            }
        }

        val videoSummary = videoSummaryPersistencePort.save(VideoSummary.create(youtubeUrl))

        Events.raise(VideoAnalyzeEvent(videoSummary.id, youtubeUrl))

        return videoSummary
    }

    private fun validateYoutubeUrl(url: String) {
        val isYoutubeUrl =
            url.contains("youtube.com") || url.contains("youtu.be")
        if (!isYoutubeUrl) {
            throw LinktripException(ExceptionCode.INVALID_YOUTUBE_URL)
        }
    }
}
