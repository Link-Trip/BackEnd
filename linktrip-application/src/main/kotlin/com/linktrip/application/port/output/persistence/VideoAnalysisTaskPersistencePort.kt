package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.CostBasis
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus

interface VideoAnalysisTaskPersistencePort {
    fun save(videoAnalysisTask: VideoAnalysisTask): VideoAnalysisTask

    fun findByYoutubeUrl(youtubeUrl: String): VideoAnalysisTask?

    fun findById(id: String): VideoAnalysisTask?

    fun updateStatus(
        id: String,
        status: VideoAnalysisTaskStatus,
    )

    fun updateValidAndStatus(
        id: String,
        valid: Boolean,
        status: VideoAnalysisTaskStatus,
        estimatedMinCost: Long? = null,
        estimatedMaxCost: Long? = null,
        costBasis: CostBasis? = null,
    )
}
