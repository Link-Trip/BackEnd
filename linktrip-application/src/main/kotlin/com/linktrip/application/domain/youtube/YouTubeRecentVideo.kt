package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.video.VideoAnalysisTask

data class YouTubeRecentVideo(
    val id: String,
    val channelId: String,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val publishedAt: String,
) {
    val videoUrl: String
        get() = VideoAnalysisTask.buildUrl(videoId)

    companion object {
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
