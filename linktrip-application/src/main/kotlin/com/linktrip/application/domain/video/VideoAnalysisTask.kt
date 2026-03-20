package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator

data class VideoAnalysisTask(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: VideoAnalysisTaskStatus,
) {
    companion object {
        fun create(youtubeUrl: String): VideoAnalysisTask =
            VideoAnalysisTask(
                id = IdGenerator.generate(),
                youtubeUrl = youtubeUrl,
                valid = false,
                status = VideoAnalysisTaskStatus.PENDING,
            )
    }
}
