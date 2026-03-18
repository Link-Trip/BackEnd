package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.common.IdGenerator

data class YouTubeVideoDetail(
    val id: String,
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val viewCount: Long,
    val likeCount: Long,
    val duration: String,
    val publishedAt: String,
    val region: String,
    val country: String,
    val city: String?,
    val theme: String?,
) {
    companion object {
        fun create(
            videoId: String,
            title: String,
            description: String,
            thumbnailUrl: String,
            channelId: String,
            channelTitle: String,
            viewCount: Long,
            likeCount: Long,
            duration: String,
            publishedAt: String,
            region: String,
            country: String,
            city: String?,
            theme: String?,
        ): YouTubeVideoDetail =
            YouTubeVideoDetail(
                id = IdGenerator.generate(),
                videoId = videoId,
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                channelId = channelId,
                channelTitle = channelTitle,
                viewCount = viewCount,
                likeCount = likeCount,
                duration = duration,
                publishedAt = publishedAt,
                region = region,
                country = country,
                city = city,
                theme = theme,
            )
    }
}
