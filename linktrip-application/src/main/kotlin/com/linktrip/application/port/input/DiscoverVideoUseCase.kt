package com.linktrip.application.port.input

import com.linktrip.application.domain.youtube.YouTubeVideoDetail

interface DiscoverVideoUseCase {
    fun getVideos(): List<YouTubeVideoDetail>

    fun getVideosByCountry(country: String): List<YouTubeVideoDetail>

    fun getVideosByRegion(region: String): List<YouTubeVideoDetail>
}
