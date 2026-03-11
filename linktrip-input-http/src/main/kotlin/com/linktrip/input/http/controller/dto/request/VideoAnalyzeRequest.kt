package com.linktrip.input.http.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class VideoAnalyzeRequest(
    @field:NotBlank
    val youtubeUrl: String,
)
