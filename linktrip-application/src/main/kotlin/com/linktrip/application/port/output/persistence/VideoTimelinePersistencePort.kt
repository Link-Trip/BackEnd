package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.VideoTimeline

interface VideoTimelinePersistencePort {
    fun saveAll(timelines: List<VideoTimeline>)

    fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<VideoTimeline>
}
