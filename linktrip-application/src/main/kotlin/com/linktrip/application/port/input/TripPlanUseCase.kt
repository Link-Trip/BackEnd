package com.linktrip.application.port.input

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.trip.TripPlan
import com.linktrip.application.domain.trip.TripPlanItem
import com.linktrip.application.domain.video.TravelItineraryItem
import java.time.LocalDateTime

interface TripPlanUseCase {
    fun registerRequest(
        memberId: String,
        videoAnalysisTaskId: String,
    )

    fun createFromAnalysisIfAbsent(
        memberId: String,
        videoAnalysisTaskId: String,
        title: String = "여행 계획",
    )

    fun getTripPlans(
        memberId: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<TripPlanSummary>

    fun getTripPlanDetail(
        memberId: String,
        tripPlanId: String,
    ): TripPlanDetail

    fun updateTripPlan(
        memberId: String,
        tripPlanId: String,
        command: UpdateTripPlanCommand,
    ): TripPlanDetail

    fun deleteTripPlan(
        memberId: String,
        tripPlanId: String,
    )
}

data class UpdateTripPlanCommand(
    val title: String?,
    val items: List<UpdateTripPlanItemCommand>?,
)

data class UpdateTripPlanItemCommand(
    val tripPlanItemId: String,
    val day: Int,
    val itemOrder: Int,
)

data class TripPlanSummary(
    val tripPlan: TripPlan,
    val youtubeUrl: String,
    val itemCount: Int,
)

data class TripPlanDetail(
    val tripPlan: TripPlan,
    val items: List<TripPlanItemDetail>,
)

data class TripPlanItemDetail(
    val tripPlanItem: TripPlanItem,
    val travelItineraryItem: TravelItineraryItem,
)
