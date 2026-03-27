package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.VideoAnalysisTaskHashtag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "video_analysis_task_hashtag",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_task_hashtag",
            columnNames = ["video_analysis_task_id", "hashtag_id"],
        ),
    ],
)
class VideoAnalysisTaskHashtagEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "video_analysis_task_id", nullable = false, length = 36)
    val videoAnalysisTaskId: String,
    @Column(name = "hashtag_id", nullable = false, length = 36)
    val hashtagId: String,
) : BaseTimeEntity() {
    companion object {
        fun from(domain: VideoAnalysisTaskHashtag): VideoAnalysisTaskHashtagEntity =
            VideoAnalysisTaskHashtagEntity(
                id = domain.id,
                videoAnalysisTaskId = domain.videoAnalysisTaskId,
                hashtagId = domain.hashtagId,
            )
    }
}
