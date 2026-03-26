package com.linktrip.output.persistence.mysql.repository.dto

import com.linktrip.output.persistence.mysql.entity.TripPlanEntity

data class TripPlanSummaryWithVideo(
    val tripPlan: TripPlanEntity,
    val youtubeUrl: String,
    val activeItemCount: Long,
)
