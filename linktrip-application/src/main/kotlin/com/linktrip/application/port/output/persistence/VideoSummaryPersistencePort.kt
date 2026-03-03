package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.VideoAnalysisResult
import com.linktrip.application.domain.video.VideoSummary
import com.linktrip.application.domain.video.VideoSummaryStatus

interface VideoSummaryPersistencePort {
    fun save(videoSummary: VideoSummary): VideoSummary

    fun findByYoutubeUrl(youtubeUrl: String): VideoSummary?

    fun findById(id: String): VideoSummary?

    fun updateSummaryAndStatus(
        id: String,
        summary: VideoAnalysisResult,
        status: VideoSummaryStatus,
    )

    fun updateStatus(
        id: String,
        status: VideoSummaryStatus,
    )
}
