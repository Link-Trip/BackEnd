package com.linktrip.input.http.controller.discover

import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.DiscoverVideoResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/discover")
class DiscoverController(
    private val discoverVideoUseCase: DiscoverVideoUseCase,
) {
    @GetMapping("/videos")
    fun getVideos(
        @RequestParam(required = false) country: String?,
        @RequestParam(required = false) region: String?,
    ): ApiResponse<List<DiscoverVideoResponse>> {
        val videos =
            when {
                country != null -> discoverVideoUseCase.getVideosByCountry(country)
                region != null -> discoverVideoUseCase.getVideosByRegion(region)
                else -> discoverVideoUseCase.getVideos()
            }
        return ApiResponse.ok(videos.map { DiscoverVideoResponse.from(it) })
    }
}
