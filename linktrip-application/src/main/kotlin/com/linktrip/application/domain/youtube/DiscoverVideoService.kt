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
    override fun getVideos(): List<YouTubeVideoDetail> = youTubeVideoPersistencePort.findAll()

    override fun getVideosByCountry(country: String): List<YouTubeVideoDetail> =
        youTubeVideoPersistencePort.findAllByCountry(country)

    override fun getVideosByRegion(region: String): List<YouTubeVideoDetail> =
        youTubeVideoPersistencePort.findAllByRegion(region)

    override fun getVideosByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoDetail> = youTubeVideoPersistencePort.findAllByTheme(theme, cursor, size)
}
