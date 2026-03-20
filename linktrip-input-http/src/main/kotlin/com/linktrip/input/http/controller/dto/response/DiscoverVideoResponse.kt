package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta

data class DiscoverVideoResponse(
    val videoId: String,
    val videoUrl: String,
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
        private const val YOUTUBE_VIDEO_URL_PREFIX = "https://www.youtube.com/watch?v="

        fun from(detail: YouTubeVideoMeta): DiscoverVideoResponse =
            DiscoverVideoResponse(
                videoId = detail.videoId,
                videoUrl = "$YOUTUBE_VIDEO_URL_PREFIX${detail.videoId}",
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

data class DiscoverVideoResponses(
    val videos: List<DiscoverVideoResponse>,
) {
    companion object {
        fun from(details: List<YouTubeVideoMeta>): DiscoverVideoResponses =
            DiscoverVideoResponses(
                videos = details.map { DiscoverVideoResponse.from(it) },
            )
    }
}

data class DiscoverVideoCursorResponse(
    val videos: List<DiscoverVideoResponse>,
    val nextCursor: String?,
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: CursorPage<YouTubeVideoMeta>): DiscoverVideoCursorResponse =
            DiscoverVideoCursorResponse(
                videos = page.items.map { DiscoverVideoResponse.from(it) },
                nextCursor = page.nextCursor,
                hasNext = page.hasNext,
            )
    }
}
