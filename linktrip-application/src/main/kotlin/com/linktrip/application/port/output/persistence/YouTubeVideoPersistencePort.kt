package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.youtube.YouTubeVideoDetail

interface YouTubeVideoPersistencePort {
    fun saveAll(videos: List<YouTubeVideoDetail>)

    fun findAll(): List<YouTubeVideoDetail>
}
