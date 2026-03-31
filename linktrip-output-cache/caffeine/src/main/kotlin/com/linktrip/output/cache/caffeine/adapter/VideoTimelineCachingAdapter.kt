package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.video.VideoTimeline
import com.linktrip.application.port.output.persistence.VideoTimelinePersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class VideoTimelineCachingAdapter(
    @param:Qualifier("videoTimelineDbAdapter")
    private val delegate: VideoTimelinePersistencePort,
) : VideoTimelinePersistencePort {
    override fun saveAll(timelines: List<VideoTimeline>) {
        delegate.saveAll(timelines)
    }

    @Cacheable(
        value = [CacheConfig.VIDEO_SCHEDULE],
        key = "'timeline:' + #videoAnalysisTaskId",
        unless = "#result.isEmpty()",
    )
    override fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<VideoTimeline> =
        delegate.findByVideoAnalysisTaskId(videoAnalysisTaskId)
}
