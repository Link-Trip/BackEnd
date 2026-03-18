package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "youtube_video",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_youtube_video_video_id", columnNames = ["video_id"]),
    ],
)
class YouTubeVideoEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "video_id", nullable = false, length = 32)
    val videoId: String,
    @Column(name = "title", nullable = false, length = 500)
    var title: String,
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String,
    @Column(name = "thumbnail_url", nullable = false, length = 1000)
    var thumbnailUrl: String,
    @Column(name = "channel_id", nullable = false, length = 64)
    var channelId: String,
    @Column(name = "channel_title", nullable = false, length = 500)
    var channelTitle: String,
    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0L,
    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0L,
    @Column(name = "duration", nullable = false, length = 32)
    var duration: String,
    @Column(name = "published_at", nullable = false, length = 64)
    var publishedAt: String,
) : BaseTimeEntity() {
    fun toDomain(): YouTubeVideoDetail =
        YouTubeVideoDetail(
            videoId = this.videoId,
            title = this.title,
            description = this.description,
            thumbnailUrl = this.thumbnailUrl,
            channelId = this.channelId,
            channelTitle = this.channelTitle,
            viewCount = this.viewCount,
            likeCount = this.likeCount,
            duration = this.duration,
            publishedAt = this.publishedAt,
        )

    fun updateFrom(detail: YouTubeVideoDetail) {
        this.title = detail.title
        this.description = detail.description
        this.thumbnailUrl = detail.thumbnailUrl
        this.channelId = detail.channelId
        this.channelTitle = detail.channelTitle
        this.viewCount = detail.viewCount
        this.likeCount = detail.likeCount
        this.duration = detail.duration
        this.publishedAt = detail.publishedAt
    }

    companion object {
        fun from(
            id: String,
            detail: YouTubeVideoDetail,
        ): YouTubeVideoEntity =
            YouTubeVideoEntity(
                id = id,
                videoId = detail.videoId,
                title = detail.title,
                description = detail.description,
                thumbnailUrl = detail.thumbnailUrl,
                channelId = detail.channelId,
                channelTitle = detail.channelTitle,
                viewCount = detail.viewCount,
                likeCount = detail.likeCount,
                duration = detail.duration,
                publishedAt = detail.publishedAt,
            )
    }
}
