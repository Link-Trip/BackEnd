package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator

data class VideoSummary(
    val id: String,
    val youtubeUrl: String,
    val summary: VideoAnalysisResult?,
    val status: VideoSummaryStatus,
) {
    companion object {
        fun create(youtubeUrl: String): VideoSummary =
            VideoSummary(
                id = IdGenerator.generate(),
                youtubeUrl = youtubeUrl,
                summary = null,
                status = VideoSummaryStatus.PENDING,
            )
    }
}
