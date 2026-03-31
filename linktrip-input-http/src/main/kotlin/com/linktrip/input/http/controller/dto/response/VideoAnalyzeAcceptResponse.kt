package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.VideoAnalysisTask
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "영상 분석 요청 접수 응답")
data class VideoAnalyzeAcceptResponse(
    @field:Schema(description = "영상 분석 작업 ID (이 ID로 schedule API 폴링)", example = "019d41ff-fae2-7d90-96c9-2530a95f64cf")
    val id: String,
    @field:Schema(description = "정규화된 YouTube URL", example = "https://www.youtube.com/watch?v=2oLfUjAqEcM")
    val youtubeUrl: String,
    @field:Schema(
        description = "분석 상태: PENDING(분석 중), COMPLETED(완료), INVALID(여행 영상 아님), FAILED(실패)",
        example = "PENDING",
        allowableValues = ["PENDING", "COMPLETED", "INVALID", "FAILED"],
    )
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
