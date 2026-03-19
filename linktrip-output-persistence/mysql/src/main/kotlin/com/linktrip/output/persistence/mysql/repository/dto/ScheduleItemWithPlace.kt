package com.linktrip.output.persistence.mysql.repository.dto

import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity

data class ScheduleItemWithPlace(
    val item: VideoScheduleItemEntity,
    val place: PlaceEntity?,
)
