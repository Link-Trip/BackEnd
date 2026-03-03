package com.linktrip.application.domain.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service

@Service
class VideoAnalyzeService(
    private val videoAnalyzePort: VideoAnalyzePort,
) : VideoAnalyzeUseCase {
    override fun analyzeVideo(youtubeUrl: String): VideoAnalysisResult {
        validateYoutubeUrl(youtubeUrl)

        val result = videoAnalyzePort.analyze(youtubeUrl)

        if (!result.valid) {
            throw LinktripException(ExceptionCode.INVALID_VIDEO)
        }

        return result
    }

    private fun validateYoutubeUrl(url: String) {
        val isYoutubeUrl =
            url.contains("youtube.com") || url.contains("youtu.be")
        if (!isYoutubeUrl) {
            throw LinktripException(ExceptionCode.INVALID_YOUTUBE_URL)
        }
    }
}
