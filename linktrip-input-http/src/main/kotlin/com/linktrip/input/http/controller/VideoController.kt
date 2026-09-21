package com.linktrip.input.http.controller

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.application.port.input.DiscoverChannelUseCase
import com.linktrip.application.port.input.DiscoverCountryUseCase
import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.application.port.input.TripPlanUseCase
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.input.VideoScheduleUseCase
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.config.PaginationDefaults
import com.linktrip.input.http.controller.docs.VideoDocs
import com.linktrip.input.http.controller.dto.request.VideoAnalyzeRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.DiscoverChannelResponses
import com.linktrip.input.http.controller.dto.response.DiscoverCountryResponses
import com.linktrip.input.http.controller.dto.response.DiscoverVideoCursorResponse
import com.linktrip.input.http.controller.dto.response.DiscoverVideoResponses
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/video")
class VideoController(
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
    private val videoScheduleUseCase: VideoScheduleUseCase,
    private val discoverVideoUseCase: DiscoverVideoUseCase,
    private val discoverChannelUseCase: DiscoverChannelUseCase,
    private val discoverCountryUseCase: DiscoverCountryUseCase,
    private val tripPlanUseCase: TripPlanUseCase,
) : VideoDocs {
    @PostMapping("/analyze")
    override fun analyzeVideo(
        @AuthenticatedMember memberId: String,
        @Validated @RequestBody request: VideoAnalyzeRequest,
    ): ApiResponse<VideoAnalyzeResponse> {
        val task = videoAnalyzeUseCase.analyzeVideo(request.youtubeUrl, Source.USER)

        when (task.status) {
            VideoAnalysisTaskStatus.COMPLETED ->
                tripPlanUseCase.createFromAnalysisIfAbsent(memberId, task.id)
            VideoAnalysisTaskStatus.INVALID -> {}
            else -> tripPlanUseCase.registerRequest(memberId, task.id)
        }

        // COMPLETED 면 결과 인라인 반환해서 클라이언트가 추가 폴링 없이 바로 사용. 그 외엔 status 만 의미 있음.
        val response =
            if (task.status == VideoAnalysisTaskStatus.COMPLETED) {
                val result = videoScheduleUseCase.getVideoSchedule(task.id)
                VideoAnalyzeResponse.from(result.videoAnalysisTask, result.items, result.timelines)
            } else {
                VideoAnalyzeResponse.from(task, emptyList(), emptyList())
            }

        return when (task.status) {
            VideoAnalysisTaskStatus.PENDING,
            VideoAnalysisTaskStatus.PROCESSING,
            -> ApiResponse.accepted(response)
            else -> ApiResponse.ok(response)
        }
    }

    @GetMapping("/schedule/{videoAnalysisTaskId}")
    override fun getVideoSchedule(
        @AuthenticatedMember memberId: String,
        @PathVariable videoAnalysisTaskId: String,
    ): ApiResponse<VideoAnalyzeResponse> {
        val result = videoScheduleUseCase.getVideoSchedule(videoAnalysisTaskId)

        if (result.videoAnalysisTask.status == VideoAnalysisTaskStatus.COMPLETED) {
            tripPlanUseCase.createFromAnalysisIfAbsent(memberId, videoAnalysisTaskId)
        }

        val response = VideoAnalyzeResponse.from(result.videoAnalysisTask, result.items, result.timelines)
        return when (result.videoAnalysisTask.status) {
            VideoAnalysisTaskStatus.PENDING,
            VideoAnalysisTaskStatus.PROCESSING,
            -> ApiResponse.accepted(response)
            else -> ApiResponse.ok(response)
        }
    }

    @GetMapping("/discover/category")
    override fun getVideos(
        @RequestParam(required = false) country: String?,
        @RequestParam(required = false) region: String?,
    ): ApiResponse<DiscoverVideoResponses> {
        val videos =
            when {
                !country.isNullOrBlank() && !region.isNullOrBlank() ->
                    throw LinktripException(ExceptionCode.BAD_REQUEST_DISCOVER_QUERY)
                !country.isNullOrBlank() -> discoverVideoUseCase.getVideosByCountry(country.trim())
                !region.isNullOrBlank() -> discoverVideoUseCase.getVideosByRegion(region.trim())
                else -> discoverVideoUseCase.getVideos()
            }
        return ApiResponse.ok(DiscoverVideoResponses.from(videos))
    }

    @GetMapping("/discover/channels")
    override fun getChannels(): ApiResponse<DiscoverChannelResponses> {
        val channels = discoverChannelUseCase.getChannels()
        return ApiResponse.ok(DiscoverChannelResponses.from(channels))
    }

    @GetMapping("/discover/countries")
    override fun getTopCountries(): ApiResponse<DiscoverCountryResponses> {
        val countries = discoverCountryUseCase.getTopCountries()
        return ApiResponse.ok(DiscoverCountryResponses.from(countries))
    }

    @GetMapping("/discover/theme")
    override fun getVideosByTheme(
        @RequestParam theme: String,
        @RequestParam(required = false) cursor: String?,
    ): ApiResponse<DiscoverVideoCursorResponse> {
        val cursorDateTime = cursor?.let { LocalDateTime.parse(it) }
        val result =
            discoverVideoUseCase.getVideosByTheme(theme.trim(), cursorDateTime, PaginationDefaults.DISCOVER_VIDEO_SIZE)
        return ApiResponse.ok(DiscoverVideoCursorResponse.from(result))
    }
}
