package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QHashtagEntity
import com.linktrip.output.persistence.mysql.entity.QVideoAnalysisTaskHashtagEntity
import com.linktrip.output.persistence.mysql.repository.dto.TaskHashtagRow
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class VideoAnalysisTaskHashtagQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val taskHashtag = QVideoAnalysisTaskHashtagEntity.videoAnalysisTaskHashtagEntity
    private val hashtag = QHashtagEntity.hashtagEntity

    fun findByVideoAnalysisTaskIds(videoAnalysisTaskIds: List<String>): List<TaskHashtagRow> {
        if (videoAnalysisTaskIds.isEmpty()) return emptyList()

        return queryFactory
            .select(
                Projections.constructor(
                    TaskHashtagRow::class.java,
                    taskHashtag.videoAnalysisTaskId,
                    hashtag.name,
                ),
            )
            .from(taskHashtag)
            .join(hashtag).on(taskHashtag.hashtagId.eq(hashtag.id))
            .where(taskHashtag.videoAnalysisTaskId.`in`(videoAnalysisTaskIds))
            .fetch()
    }
}
