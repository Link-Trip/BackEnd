package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class TravelItineraryItemCachingAdapter(
    @param:Qualifier("travelItineraryItemDbAdapter")
    private val delegate: TravelItineraryItemPersistencePort,
) : TravelItineraryItemPersistencePort {
    override fun saveAll(items: List<TravelItineraryItem>) {
        delegate.saveAll(items)
    }

    override fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        delegate.findByVideoAnalysisTaskId(videoAnalysisTaskId)

    @Cacheable(
        value = [CacheConfig.VIDEO_SCHEDULE],
        key = "#videoAnalysisTaskId",
        unless = "#result.isEmpty()",
    )
    override fun findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        delegate.findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId)

    override fun findRetryableItems(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        delegate.findRetryableItems(videoAnalysisTaskId)

    override fun findVideoAnalysisTaskIdsWithRetryableItems(): List<String> =
        delegate.findVideoAnalysisTaskIdsWithRetryableItems()
}
