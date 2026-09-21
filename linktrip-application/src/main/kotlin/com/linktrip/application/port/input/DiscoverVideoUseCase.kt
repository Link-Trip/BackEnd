package com.linktrip.application.port.input

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import java.time.LocalDateTime

interface DiscoverVideoUseCase {
    fun getVideos(size: Int? = null): List<YouTubeVideoMeta>

    fun getVideosByCountry(
        country: String,
        size: Int? = null,
    ): List<YouTubeVideoMeta>

    fun getVideosByRegion(
        region: String,
        size: Int? = null,
    ): List<YouTubeVideoMeta>

    fun getVideosByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta>
}
