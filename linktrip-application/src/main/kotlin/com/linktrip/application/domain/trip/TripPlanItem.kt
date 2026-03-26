package com.linktrip.application.domain.trip

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class TripPlanItem(
    val id: String,
    val tripPlanId: String,
    val travelItineraryItemId: String,
    val day: Int,
    val itemOrder: Int,
    val deleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            tripPlanId: String,
            travelItineraryItemId: String,
            day: Int,
            itemOrder: Int,
        ): TripPlanItem =
            TripPlanItem(
                id = IdGenerator.generate(),
                tripPlanId = tripPlanId,
                travelItineraryItemId = travelItineraryItemId,
                day = day,
                itemOrder = itemOrder,
            )
    }
}
