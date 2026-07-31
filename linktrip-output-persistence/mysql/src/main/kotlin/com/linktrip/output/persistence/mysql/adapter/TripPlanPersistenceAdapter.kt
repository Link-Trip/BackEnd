package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.trip.TripPlan
import com.linktrip.application.port.output.persistence.DestinationTripPlanCount
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanSummaryRow
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.persistence.mysql.entity.TripPlanEntity
import com.linktrip.output.persistence.mysql.repository.TripPlanJpaRepository
import com.linktrip.output.persistence.mysql.repository.TripPlanQuerydslRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TripPlanPersistenceAdapter(
    private val tripPlanJpaRepository: TripPlanJpaRepository,
    private val tripPlanQuerydslRepository: TripPlanQuerydslRepository,
) : TripPlanPersistencePort {
    override fun save(tripPlan: TripPlan): TripPlan =
        tripPlanJpaRepository.save(TripPlanEntity.from(tripPlan)).toDomain()

    override fun findById(id: String): TripPlan? = tripPlanJpaRepository.findByIdAndDeletedFalse(id)?.toDomain()

    override fun findByMemberId(memberId: String): List<TripPlan> =
        tripPlanJpaRepository.findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId).map { it.toDomain() }

    override fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean =
        tripPlanJpaRepository.existsByMemberIdAndVideoAnalysisTaskIdAndDeletedFalse(
            memberId,
            videoAnalysisTaskId,
        )

    override fun findSummariesByMemberId(
        memberId: String,
        cursor: java.time.LocalDateTime?,
        size: Int,
    ): List<TripPlanSummaryRow> =
        tripPlanQuerydslRepository.findSummariesByMemberId(memberId, cursor, size).map { row ->
            TripPlanSummaryRow(
                tripPlan = row.tripPlan.toDomain(),
                youtubeUrl = row.youtubeUrl,
                activeItemCount = row.activeItemCount.toInt(),
                days = row.days,
            )
        }

    @Transactional
    override fun deleteById(id: String) {
        val entity =
            tripPlanJpaRepository.findByIdAndDeletedFalse(id)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND_TRIP_PLAN)
        entity.softDelete()
    }

    @Transactional
    override fun updateTitle(
        id: String,
        title: String,
    ) {
        val entity =
            tripPlanJpaRepository.findByIdAndDeletedFalse(id)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND_TRIP_PLAN)
        entity.title = title
    }

    override fun countGroupedByDestination(): List<DestinationTripPlanCount> =
        tripPlanQuerydslRepository.countGroupedByDestination()
}
