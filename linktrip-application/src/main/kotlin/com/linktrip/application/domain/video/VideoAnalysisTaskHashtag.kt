package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class VideoAnalysisTaskHashtag(
    val id: String,
    val videoAnalysisTaskId: String,
    val hashtagId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            videoAnalysisTaskId: String,
            hashtagId: String,
        ): VideoAnalysisTaskHashtag =
            VideoAnalysisTaskHashtag(
                id = IdGenerator.generate(),
                videoAnalysisTaskId = videoAnalysisTaskId,
                hashtagId = hashtagId,
            )
    }
}
