package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import java.time.LocalDateTime

interface YouTubeVideoPersistencePort {
    fun saveAll(videos: List<YouTubeVideoMeta>)

    fun findExistingVideoIds(videoIds: List<String>): Set<String>

    fun findAll(): List<YouTubeVideoMeta>

    fun findAllByCountry(country: String): List<YouTubeVideoMeta>

    fun findAllByRegion(region: String): List<YouTubeVideoMeta>

    fun findAllByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta>
}
