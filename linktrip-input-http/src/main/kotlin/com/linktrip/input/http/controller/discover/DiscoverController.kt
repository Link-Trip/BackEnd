package com.linktrip.input.http.controller.discover

import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.DiscoverVideoResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

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
                !country.isNullOrBlank() && !region.isNullOrBlank() ->
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "country와 region은 동시에 사용할 수 없습니다.")
                !country.isNullOrBlank() -> discoverVideoUseCase.getVideosByCountry(country.trim())
                !region.isNullOrBlank() -> discoverVideoUseCase.getVideosByRegion(region.trim())
                else -> discoverVideoUseCase.getVideos()
            }
        return ApiResponse.ok(videos.map { DiscoverVideoResponse.from(it) })
    }
}
