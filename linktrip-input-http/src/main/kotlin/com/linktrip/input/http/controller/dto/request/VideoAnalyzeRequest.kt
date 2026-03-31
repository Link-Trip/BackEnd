package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "영상 분석 요청")
data class VideoAnalyzeRequest(
    @field:NotBlank
    @field:Schema(
        description = "분석할 YouTube URL (다양한 형식 지원: watch, youtu.be, shorts, embed)",
        example = "https://www.youtube.com/watch?v=2oLfUjAqEcM",
    )
    val youtubeUrl: String,
)
