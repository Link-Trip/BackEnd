package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.youtube.YouTubeChannelDetail

interface YouTubeChannelPersistencePort {
    fun saveAll(channels: List<YouTubeChannelDetail>)

    fun findAll(): List<YouTubeChannelDetail>
}
