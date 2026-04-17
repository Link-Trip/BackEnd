package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Primary
@Component
class YouTubeVideoCachingAdapter(
    @param:Qualifier("youtubeVideoDbAdapter")
    private val delegate: YouTubeVideoPersistencePort,
) : YouTubeVideoPersistencePort {
    @CacheEvict(value = [CacheConfig.DISCOVER_VIDEOS], allEntries = true)
    override fun saveAll(videos: List<YouTubeVideoMeta>) {
        delegate.saveAll(videos)
    }

    override fun findExistingVideoIds(videoIds: List<String>): Set<String> = delegate.findExistingVideoIds(videoIds)

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS])
    override fun findAll(): List<YouTubeVideoMeta> = delegate.findAll()

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS], key = "'country:' + #country")
    override fun findAllByCountry(country: String): List<YouTubeVideoMeta> = delegate.findAllByCountry(country)

    @Cacheable(value = [CacheConfig.DISCOVER_VIDEOS], key = "'region:' + #region")
    override fun findAllByRegion(region: String): List<YouTubeVideoMeta> = delegate.findAllByRegion(region)

    override fun findAllByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta> = delegate.findAllByTheme(theme, cursor, size)

    // backfill 스케줄러 전용 — 매번 최신 미처리 영상 조회 필요하므로 캐시 안 함
    override fun findUnanalyzedVideoIds(limit: Int): List<String> = delegate.findUnanalyzedVideoIds(limit)
}

