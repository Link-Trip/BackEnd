package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class YouTubeVideoCachingAdapter(
    @param:Qualifier("youtubeVideoDbAdapter")
    private val delegate: YouTubeVideoPersistencePort,
) : YouTubeVideoPersistencePort {
    @CacheEvict(value = [CacheConfig.DISCOVER_VIDEOS], allEntries = true)
    override fun saveAll(videos: List<YouTubeVideoDetail>) {
        delegate.saveAll(videos)
    }

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS])
    override fun findAll(): List<YouTubeVideoDetail> = delegate.findAll()
}
