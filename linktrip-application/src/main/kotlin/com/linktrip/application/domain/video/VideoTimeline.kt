package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class VideoTimeline(
    val id: String,
    val videoAnalysisTaskId: String,
    val timestampSeconds: Int,
    val description: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            videoAnalysisTaskId: String,
            timestampSeconds: Int,
            description: String,
        ): VideoTimeline =
            VideoTimeline(
                id = IdGenerator.generate(),
                videoAnalysisTaskId = videoAnalysisTaskId,
                timestampSeconds = timestampSeconds,
                description = description,
            )
    }
}
