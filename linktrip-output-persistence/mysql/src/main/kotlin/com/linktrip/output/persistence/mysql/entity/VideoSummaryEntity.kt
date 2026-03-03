package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.VideoSummaryStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "video_summary",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_video_summary_youtube_url", columnNames = ["youtube_url"]),
    ],
    indexes = [
        Index(name = "idx_video_summary_youtube_url", columnList = "youtube_url"),
    ],
)
class VideoSummaryEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "youtube_url", nullable = false, length = 512)
    val youtubeUrl: String,
    @Column(name = "summary", columnDefinition = "TEXT")
    var summary: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: VideoSummaryStatus = VideoSummaryStatus.PENDING,
) : BaseTimeEntity()
