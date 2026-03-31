package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QVideoTimelineEntity
import com.linktrip.output.persistence.mysql.entity.VideoTimelineEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class VideoTimelineQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val timeline = QVideoTimelineEntity.videoTimelineEntity

    fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<VideoTimelineEntity> =
        queryFactory
            .selectFrom(timeline)
            .where(
                timeline.videoAnalysisTaskId.eq(videoAnalysisTaskId),
                timeline.deleted.isFalse,
            )
            .orderBy(timeline.timestampSeconds.asc())
            .fetch()
}
