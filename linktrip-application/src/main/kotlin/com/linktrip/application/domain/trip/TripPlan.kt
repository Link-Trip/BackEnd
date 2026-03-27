package com.linktrip.application.domain.trip

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class TripPlan(
    val id: String,
    val memberId: String,
    val videoAnalysisTaskId: String,
    val title: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            memberId: String,
            videoAnalysisTaskId: String,
            title: String,
        ): TripPlan =
            TripPlan(
                id = IdGenerator.generate(),
                memberId = memberId,
                videoAnalysisTaskId = videoAnalysisTaskId,
                title = title,
            )
    }
}
