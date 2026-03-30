package com.linktrip.output.cache.caffeine.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@EnableCaching
@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager().apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .expireAfterWrite(CACHE_TTL_HOURS, TimeUnit.HOURS)
                    .maximumSize(CACHE_MAX_SIZE),
            )
            setCacheNames(listOf(DISCOVER_VIDEOS, DISCOVER_CHANNELS, VIDEO_SCHEDULE))
        }

    companion object {
        const val DISCOVER_VIDEOS = "discoverVideos"
        const val DISCOVER_CHANNELS = "discoverChannels"
        const val VIDEO_SCHEDULE = "videoSchedule"
        private const val CACHE_TTL_HOURS = 7L
        private const val CACHE_MAX_SIZE = 100L
    }
}
