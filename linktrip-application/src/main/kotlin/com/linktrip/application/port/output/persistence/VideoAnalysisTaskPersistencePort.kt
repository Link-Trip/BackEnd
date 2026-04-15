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

    fun findPendingTasks(): List<VideoAnalysisTask>

    /**
     * 앱 재시작 시 큐에 재적재할 대상: PENDING + PROCESSING.
     * PROCESSING 은 이전 프로세스가 처리 중 크래시로 남긴 상태일 수 있으므로 함께 복구한다.
     */
    fun findReloadableTasks(): List<VideoAnalysisTask>

    fun updateValidAndStatus(
        id: String,
        valid: Boolean,
        status: VideoAnalysisTaskStatus,
        summary: String? = null,
        estimatedMinCost: Long? = null,
        estimatedMaxCost: Long? = null,
        costBasis: CostBasis? = null,
        destination: String? = null,
    )
}
