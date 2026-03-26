package com.linktrip.application.domain.trip

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class TripPlanRequest(
    val id: String,
    val memberId: String,
    val videoAnalysisTaskId: String,
    val processed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            memberId: String,
            videoAnalysisTaskId: String,
        ): TripPlanRequest =
            TripPlanRequest(
                id = IdGenerator.generate(),
                memberId = memberId,
                videoAnalysisTaskId = videoAnalysisTaskId,
            )
    }
}
