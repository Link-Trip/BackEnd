package com.linktrip.input.http.controller.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.controller.dto.request.VideoAnalyzeRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/video")
class VideoController(
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
) {
    @PostMapping("/analyze")
    fun analyzeVideo(
        @AuthenticatedMember memberId: String,
        @Valid @RequestBody request: VideoAnalyzeRequest,
    ): ApiResponse<VideoAnalyzeResponse> {
        val result = videoAnalyzeUseCase.analyzeVideo(request.youtubeUrl)
        return ApiResponse.ok(VideoAnalyzeResponse.from(result))
    }
}
