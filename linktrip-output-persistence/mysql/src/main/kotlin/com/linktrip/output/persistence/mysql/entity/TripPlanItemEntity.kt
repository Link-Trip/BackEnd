package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.trip.TripPlanItem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "trip_plan_item",
    indexes = [
        Index(name = "idx_trip_plan_item_trip_plan_id", columnList = "trip_plan_id"),
    ],
)
class TripPlanItemEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "trip_plan_id", nullable = false, length = 36)
    val tripPlanId: String,
    @Column(name = "travel_itinerary_item_id", nullable = false, length = 36)
    val travelItineraryItemId: String,
    @Column(name = "day", nullable = false)
    var day: Int,
    @Column(name = "item_order", nullable = false)
    var itemOrder: Int,
    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,
) : BaseTimeEntity() {
    fun toDomain(): TripPlanItem =
        TripPlanItem(
            id = this.id,
            tripPlanId = this.tripPlanId,
            travelItineraryItemId = this.travelItineraryItemId,
            day = this.day,
            itemOrder = this.itemOrder,
            deleted = this.deleted,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(item: TripPlanItem): TripPlanItemEntity =
            TripPlanItemEntity(
                id = item.id,
                tripPlanId = item.tripPlanId,
                travelItineraryItemId = item.travelItineraryItemId,
                day = item.day,
                itemOrder = item.itemOrder,
                deleted = item.deleted,
            )
    }
}
