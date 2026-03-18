package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.common.IdGenerator

data class YouTubeRecentVideo(
    val id: String,
    val channelId: String,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val publishedAt: String,
) {
    val videoUrl: String
        get() = "$YOUTUBE_VIDEO_BASE_URL$videoId"

    companion object {
        private const val YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch?v="

        fun create(
            channelId: String,
            videoId: String,
            title: String,
            thumbnailUrl: String,
            publishedAt: String,
        ): YouTubeRecentVideo =
            YouTubeRecentVideo(
                id = IdGenerator.generate(),
                channelId = channelId,
                videoId = videoId,
                title = title,
                thumbnailUrl = thumbnailUrl,
                publishedAt = publishedAt,
            )
    }
}
