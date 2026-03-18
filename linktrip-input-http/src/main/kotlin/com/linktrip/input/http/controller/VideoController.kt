package com.linktrip.input.http.controller

import com.linktrip.application.port.input.DiscoverChannelUseCase
import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.input.VideoScheduleUseCase
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.controller.dto.request.VideoAnalyzeRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.DiscoverChannelResponses
import com.linktrip.input.http.controller.dto.response.DiscoverVideoResponses
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeAcceptResponse
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/video")
class VideoController(
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
    private val videoScheduleUseCase: VideoScheduleUseCase,
    private val discoverVideoUseCase: DiscoverVideoUseCase,
    private val discoverChannelUseCase: DiscoverChannelUseCase,
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

    @GetMapping("/discover/category")
    fun getVideos(
        @RequestParam(required = false) country: String?,
        @RequestParam(required = false) region: String?,
    ): ApiResponse<DiscoverVideoResponses> {
        val videos =
            when {
                !country.isNullOrBlank() && !region.isNullOrBlank() ->
                    throw LinktripException(
                        ExceptionCode.ILLEGAL_ARGUMENT,
                        "country와 region은 동시에 사용할 수 없습니다.",
                    )
                !country.isNullOrBlank() -> discoverVideoUseCase.getVideosByCountry(country.trim())
                !region.isNullOrBlank() -> discoverVideoUseCase.getVideosByRegion(region.trim())
                else -> discoverVideoUseCase.getVideos()
            }
        return ApiResponse.ok(DiscoverVideoResponses.from(videos))
    }

    @GetMapping("/discover/channels")
    fun getChannels(): ApiResponse<DiscoverChannelResponses> {
        val channels = discoverChannelUseCase.getChannels()
        return ApiResponse.ok(DiscoverChannelResponses.from(channels))
    }
}
