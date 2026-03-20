package com.linktrip.output.persistence.mysql.repository.dto

import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.entity.TravelItineraryItemEntity

data class ScheduleItemWithPlace(
    val item: TravelItineraryItemEntity,
    val place: PlaceEntity?,
)
