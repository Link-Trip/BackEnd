package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.port.input.TripPlanDetail
import com.linktrip.application.port.input.TripPlanSummary
import java.time.LocalDateTime

data class TripPlanSummaryResponse(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val youtubeUrl: String,
    val itemCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(summary: TripPlanSummary): TripPlanSummaryResponse =
            TripPlanSummaryResponse(
                id = summary.tripPlan.id,
                title = summary.tripPlan.title,
                videoAnalysisTaskId = summary.tripPlan.videoAnalysisTaskId,
                youtubeUrl = summary.youtubeUrl,
                itemCount = summary.itemCount,
                createdAt = summary.tripPlan.createdAt,
                updatedAt = summary.tripPlan.updatedAt,
            )
    }
}

data class TripPlanCursorResponse(
    val tripPlans: List<TripPlanSummaryResponse>,
    val nextCursor: String?,
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: CursorPage<TripPlanSummary>): TripPlanCursorResponse =
            TripPlanCursorResponse(
                tripPlans = page.items.map { TripPlanSummaryResponse.from(it) },
                nextCursor = page.nextCursor,
                hasNext = page.hasNext,
            )
    }
}

data class TripPlanDetailResponse(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val items: List<TripPlanItemDetailResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    data class TripPlanItemDetailResponse(
        val id: String,
        val travelItineraryItemId: String,
        val day: Int,
        val itemOrder: Int,
        val name: String,
        val category: String,
        val description: String?,
        val tips: String?,
        val place: VideoAnalyzeResponse.PlaceResponse?,
    )

    companion object {
        fun from(detail: TripPlanDetail): TripPlanDetailResponse =
            TripPlanDetailResponse(
                id = detail.tripPlan.id,
                title = detail.tripPlan.title,
                videoAnalysisTaskId = detail.tripPlan.videoAnalysisTaskId,
                items =
                    detail.items.map { itemDetail ->
                        TripPlanItemDetailResponse(
                            id = itemDetail.tripPlanItem.id,
                            travelItineraryItemId = itemDetail.tripPlanItem.travelItineraryItemId,
                            day = itemDetail.tripPlanItem.day,
                            itemOrder = itemDetail.tripPlanItem.itemOrder,
                            name = itemDetail.travelItineraryItem.name,
                            category = itemDetail.travelItineraryItem.category.name,
                            description = itemDetail.travelItineraryItem.description,
                            tips = itemDetail.travelItineraryItem.tips,
                            place =
                                itemDetail.travelItineraryItem.place?.let {
                                    VideoAnalyzeResponse.PlaceResponse.from(it)
                                },
                        )
                    },
                createdAt = detail.tripPlan.createdAt,
                updatedAt = detail.tripPlan.updatedAt,
            )
    }
}
