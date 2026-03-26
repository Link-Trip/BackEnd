package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QTripPlanEntity
import com.linktrip.output.persistence.mysql.entity.QTripPlanItemEntity
import com.linktrip.output.persistence.mysql.entity.QVideoAnalysisTaskEntity
import com.linktrip.output.persistence.mysql.repository.dto.TripPlanSummaryWithVideo
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class TripPlanQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val tripPlan = QTripPlanEntity.tripPlanEntity
    private val videoAnalysisTask = QVideoAnalysisTaskEntity.videoAnalysisTaskEntity
    private val tripPlanItem = QTripPlanItemEntity.tripPlanItemEntity

    fun findSummariesByMemberId(
        memberId: String,
        cursor: LocalDateTime?,
        size: Int,
    ): List<TripPlanSummaryWithVideo> {
        val activeCountSubQuery =
            JPAExpressions
                .select(tripPlanItem.count())
                .from(tripPlanItem)
                .where(
                    tripPlanItem.tripPlanId.eq(tripPlan.id),
                    tripPlanItem.deleted.isFalse,
                )

        val query =
            queryFactory
                .select(tripPlan, videoAnalysisTask.youtubeUrl, activeCountSubQuery)
                .from(tripPlan)
                .join(videoAnalysisTask).on(tripPlan.videoAnalysisTaskId.eq(videoAnalysisTask.id))
                .where(tripPlan.memberId.eq(memberId))

        if (cursor != null) {
            query.where(tripPlan.createdAt.before(cursor))
        }

        return query
            .orderBy(tripPlan.createdAt.desc())
            .limit(size.toLong())
            .fetch()
            .map { tuple ->
                TripPlanSummaryWithVideo(
                    tripPlan = tuple.get(tripPlan)!!,
                    youtubeUrl = tuple.get(videoAnalysisTask.youtubeUrl) ?: "",
                    activeItemCount = tuple.get(activeCountSubQuery) ?: 0L,
                )
            }
    }
}
