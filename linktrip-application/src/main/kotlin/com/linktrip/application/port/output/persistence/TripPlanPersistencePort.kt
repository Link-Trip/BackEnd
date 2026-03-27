package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.trip.TripPlan

interface TripPlanPersistencePort {
    fun save(tripPlan: TripPlan): TripPlan

    fun findById(id: String): TripPlan?

    fun findByMemberId(memberId: String): List<TripPlan>

    fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean

    fun findSummariesByMemberId(
        memberId: String,
        cursor: java.time.LocalDateTime?,
        size: Int,
    ): List<TripPlanSummaryRow>

    fun deleteById(id: String)

    fun updateTitle(
        id: String,
        title: String,
    )
}

data class TripPlanSummaryRow(
    val tripPlan: TripPlan,
    val youtubeUrl: String,
    val activeItemCount: Int,
    val days: Int,
)
