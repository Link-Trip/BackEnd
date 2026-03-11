package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator

data class VideoSummary(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: VideoSummaryStatus,
) {
    companion object {
        fun create(youtubeUrl: String): VideoSummary =
            VideoSummary(
                id = IdGenerator.generate(),
                youtubeUrl = youtubeUrl,
                valid = false,
                status = VideoSummaryStatus.PENDING,
            )
    }
}
