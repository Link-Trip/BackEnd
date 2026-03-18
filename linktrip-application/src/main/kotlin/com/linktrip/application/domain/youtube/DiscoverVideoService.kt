package com.linktrip.application.domain.youtube

import com.linktrip.application.port.input.DiscoverVideoUseCase
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import org.springframework.stereotype.Service

@Service
class DiscoverVideoService(
    private val youTubeVideoPersistencePort: YouTubeVideoPersistencePort,
) : DiscoverVideoUseCase {
    override fun getVideos(): List<YouTubeVideoDetail> = youTubeVideoPersistencePort.findAll()

    override fun getVideosByCountry(country: String): List<YouTubeVideoDetail> =
        youTubeVideoPersistencePort.findAllByCountry(country)

    override fun getVideosByRegion(region: String): List<YouTubeVideoDetail> =
        youTubeVideoPersistencePort.findAllByRegion(region)
}
