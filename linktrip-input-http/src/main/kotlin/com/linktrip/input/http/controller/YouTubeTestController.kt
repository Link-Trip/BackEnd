package com.linktrip.input.http.controller

import com.linktrip.application.domain.youtube.YouTubeChannelCollectService
import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeCollectService
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import com.linktrip.application.port.input.KeywordAnalyzeUseCase
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.KeywordAnalyzeResponse
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Hidden
@Profile("dev")
@RestController
@Deprecated(message = "테스트 전용")
@RequestMapping("/test/youtube")
class YouTubeTestController(
    private val youTubePort: YouTubePort,
    private val youTubeCollectService: YouTubeCollectService,
    private val youTubeChannelCollectService: YouTubeChannelCollectService,
    private val keywordAnalyzeUseCase: KeywordAnalyzeUseCase,
) {
    @GetMapping("/search/videos")
    fun searchVideos(
        @RequestParam q: String,
        @RequestParam(defaultValue = "5") maxResults: Int,
    ): ApiResponse<List<YouTubeSearchResult>> = ApiResponse.ok(youTubePort.searchVideos(q, maxResults))

    @GetMapping("/videos")
    fun getVideoDetails(
        @RequestParam ids: List<String>,
    ): ApiResponse<List<YouTubeVideoMeta>> = ApiResponse.ok(youTubePort.getVideoDetails(ids))

    @GetMapping("/search/channels")
    fun searchChannels(
        @RequestParam q: String,
        @RequestParam(defaultValue = "5") maxResults: Int,
    ): ApiResponse<List<YouTubeChannelDetail>> = ApiResponse.ok(youTubePort.searchChannels(q, maxResults))

    @GetMapping("/channels")
    fun getChannelDetails(
        @RequestParam ids: List<String>,
    ): ApiResponse<List<YouTubeChannelDetail>> = ApiResponse.ok(youTubePort.getChannelDetails(ids))

    @PostMapping("/collect")
    fun collectVideos(): ApiResponse<String> {
        youTubeCollectService.collectVideos()
        return ApiResponse.ok("YouTube 영상 수집 완료")
    }

    @PostMapping("/collect/channels")
    fun collectChannels(): ApiResponse<String> {
        youTubeChannelCollectService.collectChannels()
        return ApiResponse.ok("YouTube 채널 수집 완료")
    }

    @PostMapping("/analyze/keywords")
    fun analyzeByKeywords(
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) country: String?,
        @RequestParam(defaultValue = "5") maxResults: Int,
    ): ApiResponse<KeywordAnalyzeResponse> {
        val result = keywordAnalyzeUseCase.analyzeByKeywords(region, country, maxResults)
        return ApiResponse.ok(KeywordAnalyzeResponse.from(result))
    }
}
