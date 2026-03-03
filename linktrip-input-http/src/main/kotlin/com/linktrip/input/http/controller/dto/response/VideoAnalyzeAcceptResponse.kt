package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.VideoSummary

data class VideoAnalyzeAcceptResponse(
    val id: String,
    val youtubeUrl: String,
    val status: String,
) {
    companion object {
        fun from(videoSummary: VideoSummary): VideoAnalyzeAcceptResponse =
            VideoAnalyzeAcceptResponse(
                id = videoSummary.id,
                youtubeUrl = videoSummary.youtubeUrl,
                status = videoSummary.status.name,
            )
    }
}
