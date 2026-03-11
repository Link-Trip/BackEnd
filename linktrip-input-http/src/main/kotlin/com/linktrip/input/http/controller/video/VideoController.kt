package com.linktrip.input.http.controller.video

import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.input.VideoScheduleUseCase
import com.linktrip.input.http.controller.dto.request.VideoAnalyzeRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeAcceptResponse
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/video")
class VideoController(
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
    private val videoScheduleUseCase: VideoScheduleUseCase,
) {
    @PostMapping("/analyze")
    fun analyzeVideo(
        @Valid @RequestBody request: VideoAnalyzeRequest,
    ): ApiResponse<VideoAnalyzeAcceptResponse> {
        val videoSummary = videoAnalyzeUseCase.analyzeVideo(request.youtubeUrl)
        return ApiResponse.accepted(VideoAnalyzeAcceptResponse.from(videoSummary))
    }

    @GetMapping("/{videoSummaryId}/schedule")
    fun getVideoSchedule(
        @PathVariable videoSummaryId: String,
    ): ApiResponse<VideoAnalyzeResponse> {
        val result = videoScheduleUseCase.getVideoSchedule(videoSummaryId)
        return ApiResponse.ok(
            VideoAnalyzeResponse.from(result.videoSummary, result.items),
        )
    }
}
