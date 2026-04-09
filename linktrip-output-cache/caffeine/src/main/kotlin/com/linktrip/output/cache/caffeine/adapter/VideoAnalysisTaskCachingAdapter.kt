package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.video.CostBasis
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.output.cache.caffeine.config.CacheConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Primary
@Component
class VideoAnalysisTaskCachingAdapter(
    @param:Qualifier("videoAnalysisTaskDbAdapter")
    private val delegate: VideoAnalysisTaskPersistencePort,
) : VideoAnalysisTaskPersistencePort {
    override fun save(videoAnalysisTask: VideoAnalysisTask): VideoAnalysisTask = delegate.save(videoAnalysisTask)

    override fun findByYoutubeUrl(youtubeUrl: String): VideoAnalysisTask? = delegate.findByYoutubeUrl(youtubeUrl)

    override fun findPendingTasks(): List<VideoAnalysisTask> = delegate.findPendingTasks()

    @Cacheable(
        value = [CacheConfig.VIDEO_ANALYSIS_TASK],
        key = "#id",
        unless = "#result == null",
    )
    override fun findById(id: String): VideoAnalysisTask? = delegate.findById(id)

    @CacheEvict(value = [CacheConfig.VIDEO_ANALYSIS_TASK], key = "#id")
    override fun updateStatus(
        id: String,
        status: VideoAnalysisTaskStatus,
    ) {
        delegate.updateStatus(id, status)
    }

    @CacheEvict(value = [CacheConfig.VIDEO_ANALYSIS_TASK], key = "#id")
    override fun updateValidAndStatus(
        id: String,
        valid: Boolean,
        status: VideoAnalysisTaskStatus,
        summary: String?,
        estimatedMinCost: Long?,
        estimatedMaxCost: Long?,
        costBasis: CostBasis?,
        destination: String?,
    ) {
        delegate.updateValidAndStatus(
            id,
            valid,
            status,
            summary,
            estimatedMinCost,
            estimatedMaxCost,
            costBasis,
            destination,
        )
    }
}
