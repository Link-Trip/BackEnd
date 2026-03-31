package com.linktrip.application.port.input

import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoTimeline

interface VideoScheduleUseCase {
    fun getVideoSchedule(videoAnalysisTaskId: String): VideoScheduleResult
}

data class VideoScheduleResult(
    val videoAnalysisTask: VideoAnalysisTask,
    val items: List<TravelItineraryItem>,
    val timelines: List<VideoTimeline>,
)
