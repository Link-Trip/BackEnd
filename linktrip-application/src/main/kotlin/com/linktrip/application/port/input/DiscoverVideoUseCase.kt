package com.linktrip.application.port.input

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import java.time.LocalDateTime

interface DiscoverVideoUseCase {
    fun getVideos(): List<YouTubeVideoDetail>

    fun getVideosByCountry(country: String): List<YouTubeVideoDetail>

    fun getVideosByRegion(region: String): List<YouTubeVideoDetail>

    fun getVideosByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoDetail>
}
