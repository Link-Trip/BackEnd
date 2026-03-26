package com.linktrip.output.persistence.mysql.repository.dto

import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.entity.TravelItineraryItemEntity
import com.linktrip.output.persistence.mysql.entity.TripPlanItemEntity

data class TripPlanItemWithItineraryAndPlace(
    val tripPlanItem: TripPlanItemEntity,
    val itineraryItem: TravelItineraryItemEntity,
    val place: PlaceEntity?,
)
