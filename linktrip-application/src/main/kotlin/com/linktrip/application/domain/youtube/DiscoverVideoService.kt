package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DiscoverVideoService(
    private val youTubeVideoPersistencePort: YouTubeVideoPersistencePort,
) : DiscoverVideoUseCase {
    override fun getVideos(size: Int?): List<YouTubeVideoMeta> = youTubeVideoPersistencePort.findAll(size)

    override fun getVideosByCountry(
        country: String,
        size: Int?,
    ): List<YouTubeVideoMeta> = youTubeVideoPersistencePort.findAllByCountry(country, size)

    override fun getVideosByRegion(
        region: String,
        size: Int?,
    ): List<YouTubeVideoMeta> = youTubeVideoPersistencePort.findAllByRegion(region, size)

    override fun getVideosByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta> = youTubeVideoPersistencePort.findAllByTheme(theme, cursor, size)
}
