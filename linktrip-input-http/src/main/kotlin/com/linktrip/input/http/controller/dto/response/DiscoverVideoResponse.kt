package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.youtube.YouTubeVideoDetail

data class DiscoverVideoResponse(
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
        fun from(detail: YouTubeVideoDetail): DiscoverVideoResponse =
            DiscoverVideoResponse(
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
                region = detail.region,
                country = detail.country,
                city = detail.city,
                theme = detail.theme,
            )
    }
}
