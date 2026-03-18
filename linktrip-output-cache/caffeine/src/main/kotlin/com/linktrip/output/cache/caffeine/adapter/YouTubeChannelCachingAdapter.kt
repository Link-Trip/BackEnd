package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class YouTubeChannelCachingAdapter(
    @param:Qualifier("youtubeChannelDbAdapter")
    private val delegate: YouTubeChannelPersistencePort,
) : YouTubeChannelPersistencePort {
    @CacheEvict(value = [CacheConfig.DISCOVER_CHANNELS], allEntries = true)
    override fun saveAll(channels: List<YouTubeChannelDetail>) {
        delegate.saveAll(channels)
    }

    @Cacheable(value = [CacheConfig.DISCOVER_CHANNELS])
    override fun findAll(): List<YouTubeChannelDetail> = delegate.findAll()
}
