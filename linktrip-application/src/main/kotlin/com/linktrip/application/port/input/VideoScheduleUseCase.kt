package com.linktrip.application.port.input

import com.linktrip.application.domain.video.VideoScheduleItem
import com.linktrip.application.domain.video.VideoSummary

interface VideoScheduleUseCase {
    fun getVideoSchedule(videoSummaryId: String): VideoScheduleResult
}

data class VideoScheduleResult(
    val videoSummary: VideoSummary,
    val items: List<VideoScheduleItem>,
)
