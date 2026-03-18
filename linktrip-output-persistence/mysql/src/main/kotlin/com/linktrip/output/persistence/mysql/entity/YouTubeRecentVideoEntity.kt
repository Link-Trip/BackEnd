package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.youtube.YouTubeRecentVideo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "youtube_recent_video",
    indexes = [
        Index(name = "idx_youtube_recent_video_channel_id", columnList = "channel_id"),
    ],
)
class YouTubeRecentVideoEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "channel_id", nullable = false, length = 64)
    val channelId: String,
    @Column(name = "video_id", nullable = false, length = 64)
    val videoId: String,
    @Column(name = "title", nullable = false, length = 500)
    val title: String,
    @Column(name = "thumbnail_url", nullable = false, length = 1000)
    val thumbnailUrl: String,
    @Column(name = "published_at", nullable = false, length = 64)
    val publishedAt: String,
) {
    fun toDomain(): YouTubeRecentVideo =
        YouTubeRecentVideo(
            id = this.id,
            channelId = this.channelId,
            videoId = this.videoId,
            title = this.title,
            thumbnailUrl = this.thumbnailUrl,
            publishedAt = this.publishedAt,
        )

    companion object {
        fun from(domain: YouTubeRecentVideo): YouTubeRecentVideoEntity =
            YouTubeRecentVideoEntity(
                id = domain.id,
                channelId = domain.channelId,
                videoId = domain.videoId,
                title = domain.title,
                thumbnailUrl = domain.thumbnailUrl,
                publishedAt = domain.publishedAt,
            )
    }
}
