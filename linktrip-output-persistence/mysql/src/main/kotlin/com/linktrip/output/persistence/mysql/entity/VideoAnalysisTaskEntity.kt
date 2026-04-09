package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.CostBasis
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "video_analysis_task",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_video_analysis_task_youtube_url", columnNames = ["youtube_url"]),
    ],
)
class VideoAnalysisTaskEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "youtube_url", nullable = false, length = 512)
    val youtubeUrl: String,
    @Column(name = "valid", nullable = false)
    var valid: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: VideoAnalysisTaskStatus = VideoAnalysisTaskStatus.PENDING,
    @Column(name = "estimated_min_cost")
    var estimatedMinCost: Long? = null,
    @Column(name = "estimated_max_cost")
    var estimatedMaxCost: Long? = null,
    @Column(name = "summary", columnDefinition = "TEXT")
    var summary: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "cost_basis", length = 20)
    var costBasis: CostBasis? = null,
    @Column(name = "destination", length = 100)
    var destination: String? = null,
) : BaseTimeEntity() {
    @PreRemove
    fun preventDeletion() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    override fun softDelete() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    fun toDomain(): VideoAnalysisTask =
        VideoAnalysisTask(
            id = this.id,
            youtubeUrl = this.youtubeUrl,
            valid = this.valid,
            status = this.status,
            summary = this.summary,
            estimatedMinCost = this.estimatedMinCost,
            estimatedMaxCost = this.estimatedMaxCost,
            costBasis = this.costBasis,
            destination = this.destination,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(videoAnalysisTask: VideoAnalysisTask): VideoAnalysisTaskEntity =
            VideoAnalysisTaskEntity(
                id = videoAnalysisTask.id,
                youtubeUrl = videoAnalysisTask.youtubeUrl,
                valid = videoAnalysisTask.valid,
                status = videoAnalysisTask.status,
                summary = videoAnalysisTask.summary,
                estimatedMinCost = videoAnalysisTask.estimatedMinCost,
                estimatedMaxCost = videoAnalysisTask.estimatedMaxCost,
                costBasis = videoAnalysisTask.costBasis,
                destination = videoAnalysisTask.destination,
            )
    }
}
