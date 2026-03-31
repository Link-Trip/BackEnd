package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.VideoTimeline
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PreRemove
import jakarta.persistence.Table

@Entity
@Table(
    name = "video_timeline",
    indexes = [
        Index(
            name = "idx_video_timeline_task_deleted_timestamp",
            columnList = "video_analysis_task_id, deleted, timestamp_seconds",
        ),
    ],
)
class VideoTimelineEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "video_analysis_task_id", nullable = false, length = 36)
    val videoAnalysisTaskId: String,
    @Column(name = "timestamp_seconds", nullable = false)
    val timestampSeconds: Int,
    @Column(name = "description", nullable = false, length = 255)
    val description: String,
) : BaseTimeEntity() {
    @PreRemove
    fun preventDeletion() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    override fun softDelete() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    fun toDomain(): VideoTimeline =
        VideoTimeline(
            id = this.id,
            videoAnalysisTaskId = this.videoAnalysisTaskId,
            timestampSeconds = this.timestampSeconds,
            description = this.description,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(videoTimeline: VideoTimeline): VideoTimelineEntity =
            VideoTimelineEntity(
                id = videoTimeline.id,
                videoAnalysisTaskId = videoTimeline.videoAnalysisTaskId,
                timestampSeconds = videoTimeline.timestampSeconds,
                description = videoTimeline.description,
            )
    }
}
