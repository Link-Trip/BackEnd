package com.linktrip.input.http.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class VideoAnalyzeRequest(
    @field:NotBlank(message = "유튜브 URL은 필수입니다.")
    val youtubeUrl: String,
)
