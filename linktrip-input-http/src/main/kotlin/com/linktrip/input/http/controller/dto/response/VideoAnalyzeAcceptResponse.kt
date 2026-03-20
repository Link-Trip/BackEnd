package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.VideoAnalysisTask

data class VideoAnalyzeAcceptResponse(
    val id: String,
    val youtubeUrl: String,
    val status: String,
) {
    companion object {
        fun from(videoAnalysisTask: VideoAnalysisTask): VideoAnalyzeAcceptResponse =
            VideoAnalyzeAcceptResponse(
                id = videoAnalysisTask.id,
                youtubeUrl = videoAnalysisTask.youtubeUrl,
                status = videoAnalysisTask.status.name,
            )
    }
}
