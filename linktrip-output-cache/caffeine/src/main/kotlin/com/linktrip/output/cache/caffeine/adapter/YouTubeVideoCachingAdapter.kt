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
open class YouTubeVideoCachingAdapter(
    @param:Qualifier("youtubeVideoDbAdapter")
    private val delegate: YouTubeVideoPersistencePort,
) : YouTubeVideoPersistencePort {
    @CacheEvict(value = [CacheConfig.DISCOVER_VIDEOS], allEntries = true)
    override fun saveAll(videos: List<YouTubeVideoDetail>) {
        delegate.saveAll(videos)
    }

    override fun findExistingVideoIds(videoIds: List<String>): Set<String> =
        delegate.findExistingVideoIds(videoIds)

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS])
    override fun findAll(): List<YouTubeVideoDetail> = delegate.findAll()

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS], key = "'country:' + #country")
    override fun findAllByCountry(country: String): List<YouTubeVideoDetail> = delegate.findAllByCountry(country)

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS], key = "'region:' + #region")
    override fun findAllByRegion(region: String): List<YouTubeVideoDetail> = delegate.findAllByRegion(region)
}
