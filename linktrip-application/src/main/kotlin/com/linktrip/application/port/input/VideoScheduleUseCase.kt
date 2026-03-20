package com.linktrip.application.port.input

import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.domain.video.VideoAnalysisTask

interface VideoScheduleUseCase {
    fun getVideoSchedule(videoAnalysisTaskId: String): VideoScheduleResult
}

data class VideoScheduleResult(
    val videoAnalysisTask: VideoAnalysisTask,
    val items: List<TravelItineraryItem>,
)
