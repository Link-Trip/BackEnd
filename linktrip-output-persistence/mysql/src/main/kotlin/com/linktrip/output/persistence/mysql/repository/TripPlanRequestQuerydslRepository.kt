package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QTripPlanRequestEntity
import com.linktrip.output.persistence.mysql.entity.TripPlanRequestEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripPlanRequestQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val request = QTripPlanRequestEntity.tripPlanRequestEntity

    fun findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TripPlanRequestEntity> =
        queryFactory
            .selectFrom(request)
            .where(
                request.videoAnalysisTaskId.eq(videoAnalysisTaskId),
                request.processed.isFalse,
                request.deleted.isFalse,
            )
            .fetch()

    fun findMemberIdsByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<String> =
        queryFactory
            .selectDistinct(request.memberId)
            .from(request)
            .where(
                request.videoAnalysisTaskId.eq(videoAnalysisTaskId),
                request.deleted.isFalse,
            )
            .fetch()
}
