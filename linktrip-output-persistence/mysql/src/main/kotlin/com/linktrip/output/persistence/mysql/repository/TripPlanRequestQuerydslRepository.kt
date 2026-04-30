package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QTripPlanRequestEntity
import com.linktrip.output.persistence.mysql.entity.TripPlanRequestEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

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

    fun countByMemberIdAndDate(
        memberId: String,
        date: LocalDate,
    ): Long {
        val startOfDay = date.atStartOfDay()
        val startOfNextDay = date.plusDays(1).atStartOfDay()
        return queryFactory
            .select(request.count())
            .from(request)
            .where(
                request.memberId.eq(memberId),
                request.createdAt.goe(startOfDay),
                request.createdAt.lt(startOfNextDay),
                request.deleted.isFalse,
            )
            .fetchOne() ?: 0L
    }
}
