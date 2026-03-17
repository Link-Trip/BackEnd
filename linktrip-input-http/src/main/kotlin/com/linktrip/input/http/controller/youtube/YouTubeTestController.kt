package com.linktrip.input.http.controller.youtube

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.input.http.controller.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
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
) {
    @GetMapping("/search/videos")
    fun searchVideos(
        @RequestParam q: String,
        @RequestParam(defaultValue = "5") maxResults: Int,
    ): ApiResponse<List<YouTubeSearchResult>> = ApiResponse.ok(youTubePort.searchVideos(q, maxResults))

    @GetMapping("/videos")
    fun getVideoDetails(
        @RequestParam ids: List<String>,
    ): ApiResponse<List<YouTubeVideoDetail>> = ApiResponse.ok(youTubePort.getVideoDetails(ids))

    @GetMapping("/search/channels")
    fun searchChannels(
        @RequestParam q: String,
        @RequestParam(defaultValue = "5") maxResults: Int,
    ): ApiResponse<List<YouTubeChannelDetail>> = ApiResponse.ok(youTubePort.searchChannels(q, maxResults))

    @GetMapping("/channels")
    fun getChannelDetails(
        @RequestParam ids: List<String>,
    ): ApiResponse<List<YouTubeChannelDetail>> = ApiResponse.ok(youTubePort.getChannelDetails(ids))
}
