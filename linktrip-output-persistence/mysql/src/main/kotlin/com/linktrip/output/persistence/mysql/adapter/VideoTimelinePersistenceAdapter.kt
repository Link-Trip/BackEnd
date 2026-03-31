package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.VideoTimeline
import com.linktrip.application.port.output.persistence.VideoTimelinePersistencePort
import com.linktrip.output.persistence.mysql.entity.VideoTimelineEntity
import com.linktrip.output.persistence.mysql.repository.VideoTimelineJpaRepository
import com.linktrip.output.persistence.mysql.repository.VideoTimelineQuerydslRepository
import org.springframework.stereotype.Component

@Component("videoTimelineDbAdapter")
class VideoTimelinePersistenceAdapter(
    private val jpaRepository: VideoTimelineJpaRepository,
    private val querydslRepository: VideoTimelineQuerydslRepository,
) : VideoTimelinePersistencePort {
    override fun saveAll(timelines: List<VideoTimeline>) {
        val entities = timelines.map { VideoTimelineEntity.from(it) }
        jpaRepository.saveAll(entities)
    }

    override fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<VideoTimeline> =
        querydslRepository
            .findByVideoAnalysisTaskId(videoAnalysisTaskId)
            .map { it.toDomain() }
}
