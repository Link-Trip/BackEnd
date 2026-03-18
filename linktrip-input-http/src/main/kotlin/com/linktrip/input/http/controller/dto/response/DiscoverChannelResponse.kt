package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeRecentVideo

data class DiscoverChannelResponse(
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val recentVideos: List<RecentVideoResponse>,
) {
    data class RecentVideoResponse(
        val videoId: String,
        val title: String,
        val thumbnailUrl: String,
        val publishedAt: String,
        val videoUrl: String,
    ) {
        companion object {
            fun from(video: YouTubeRecentVideo): RecentVideoResponse =
                RecentVideoResponse(
                    videoId = video.videoId,
                    title = video.title,
                    thumbnailUrl = video.thumbnailUrl,
                    publishedAt = video.publishedAt,
                    videoUrl = video.videoUrl,
                )
        }
    }

    companion object {
        fun from(detail: YouTubeChannelDetail): DiscoverChannelResponse =
            DiscoverChannelResponse(
                channelId = detail.channelId,
                title = detail.title,
                description = detail.description,
                thumbnailUrl = detail.thumbnailUrl,
                subscriberCount = detail.subscriberCount,
                videoCount = detail.videoCount,
                recentVideos = detail.recentVideos.map { RecentVideoResponse.from(it) },
            )
    }
}

data class DiscoverChannelResponses(
    val channels: List<DiscoverChannelResponse>,
) {
    companion object {
        fun from(details: List<YouTubeChannelDetail>): DiscoverChannelResponses =
            DiscoverChannelResponses(
                channels = details.map { DiscoverChannelResponse.from(it) },
            )
    }
}
