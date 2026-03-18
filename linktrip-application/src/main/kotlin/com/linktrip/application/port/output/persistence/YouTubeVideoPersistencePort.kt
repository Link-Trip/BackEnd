package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.youtube.YouTubeVideoDetail

interface YouTubeVideoPersistencePort {
    fun saveAll(videos: List<YouTubeVideoDetail>)

    fun findExistingVideoIds(videoIds: List<String>): Set<String>

    fun findAll(): List<YouTubeVideoDetail>

    fun findAllByCountry(country: String): List<YouTubeVideoDetail>

    fun findAllByRegion(region: String): List<YouTubeVideoDetail>
}
