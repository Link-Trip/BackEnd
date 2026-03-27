package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.CostBasis
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.persistence.mysql.entity.VideoAnalysisTaskEntity
import com.linktrip.output.persistence.mysql.repository.VideoAnalysisTaskJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoAnalysisTaskAdapter(
    private val videoAnalysisTaskJpaRepository: VideoAnalysisTaskJpaRepository,
) : VideoAnalysisTaskPersistencePort {
    override fun save(videoAnalysisTask: VideoAnalysisTask): VideoAnalysisTask {
        val entity = VideoAnalysisTaskEntity.from(videoAnalysisTask)
        return videoAnalysisTaskJpaRepository.save(entity).toDomain()
    }

    override fun findByYoutubeUrl(youtubeUrl: String): VideoAnalysisTask? =
        videoAnalysisTaskJpaRepository.findByYoutubeUrl(youtubeUrl)?.toDomain()

    override fun findById(id: String): VideoAnalysisTask? =
        videoAnalysisTaskJpaRepository.findById(
            id,
        ).orElse(null)?.toDomain()

    @Transactional
    override fun updateStatus(
        id: String,
        status: VideoAnalysisTaskStatus,
    ) {
        val entity =
            videoAnalysisTaskJpaRepository.findById(id).orElseThrow {
                LinktripException(ExceptionCode.NOT_FOUND_VIDEO_ANALYSIS_TASK, "VideoAnalysisTask not found: id=$id")
            }
        entity.status = status
    }

    @Transactional
    override fun updateValidAndStatus(
        id: String,
        valid: Boolean,
        status: VideoAnalysisTaskStatus,
        estimatedMinCost: Long?,
        estimatedMaxCost: Long?,
        costBasis: CostBasis?,
    ) {
        val entity =
            videoAnalysisTaskJpaRepository.findById(id).orElseThrow {
                LinktripException(ExceptionCode.NOT_FOUND_VIDEO_ANALYSIS_TASK, "VideoAnalysisTask not found: id=$id")
            }
        entity.valid = valid
        entity.status = status
        entity.estimatedMinCost = estimatedMinCost
        entity.estimatedMaxCost = estimatedMaxCost
        entity.costBasis = costBasis
    }
}
