package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "youtube_channel",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_youtube_channel_channel_id", columnNames = ["channel_id"]),
    ],
)
class YouTubeChannelEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "channel_id", nullable = false, length = 64)
    val channelId: String,
    @Column(name = "title", nullable = false, length = 500)
    var title: String,
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String,
    @Column(name = "thumbnail_url", nullable = false, length = 1000)
    var thumbnailUrl: String,
    @Column(name = "subscriber_count", nullable = false)
    var subscriberCount: Long = 0L,
    @Column(name = "video_count", nullable = false)
    var videoCount: Long = 0L,
) : BaseTimeEntity() {
    fun toDomain(
        recentVideos: List<com.linktrip.application.domain.youtube.YouTubeRecentVideo> = emptyList(),
    ): YouTubeChannelDetail =
        YouTubeChannelDetail(
            channelId = this.channelId,
            title = this.title,
            description = this.description,
            thumbnailUrl = this.thumbnailUrl,
            subscriberCount = this.subscriberCount,
            videoCount = this.videoCount,
            recentVideos = recentVideos,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun updateFrom(detail: YouTubeChannelDetail) {
        this.title = detail.title
        this.description = detail.description
        this.thumbnailUrl = detail.thumbnailUrl
        this.subscriberCount = detail.subscriberCount
        this.videoCount = detail.videoCount
    }

    companion object {
        fun from(
            id: String,
            detail: YouTubeChannelDetail,
        ): YouTubeChannelEntity =
            YouTubeChannelEntity(
                id = id,
                channelId = detail.channelId,
                title = detail.title,
                description = detail.description,
                thumbnailUrl = detail.thumbnailUrl,
                subscriberCount = detail.subscriberCount,
                videoCount = detail.videoCount,
            )
    }
}
