package com.linktrip.application.port.output.queue

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalyzeEvent

interface VideoAnalysisQueuePort {
    fun enqueue(
        videoAnalysisTaskId: String,
        youtubeUrl: String,
        source: Source,
    )

    fun dequeue(): VideoAnalyzeEvent?

    fun size(): Int

    fun contains(videoAnalysisTaskId: String): Boolean
}
