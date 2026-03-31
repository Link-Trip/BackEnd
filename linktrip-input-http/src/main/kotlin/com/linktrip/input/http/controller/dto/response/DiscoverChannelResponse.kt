package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeRecentVideo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "탐색 채널 정보")
data class DiscoverChannelResponse(
    @field:Schema(description = "YouTube 채널 ID", example = "UCxxxxxxxxxxxxxxxxxxxxxx")
    val channelId: String,
    @field:Schema(description = "채널 이름", example = "여행 유튜버")
    val title: String,
    @field:Schema(description = "채널 설명")
    val description: String,
    @field:Schema(description = "채널 썸네일 URL")
    val thumbnailUrl: String,
    @field:Schema(description = "구독자 수", example = "500000")
    val subscriberCount: Long,
    @field:Schema(description = "총 영상 수", example = "120")
    val videoCount: Long,
    @field:Schema(description = "최신 영상 목록")
    val recentVideos: List<RecentVideoResponse>,
) {
    @Schema(description = "채널 최신 영상 정보")
    data class RecentVideoResponse(
        @field:Schema(description = "YouTube 영상 ID", example = "2oLfUjAqEcM")
        val videoId: String,
        @field:Schema(description = "영상 제목", example = "후쿠오카 1박2일 맛집 여행")
        val title: String,
        @field:Schema(description = "썸네일 URL")
        val thumbnailUrl: String,
        @field:Schema(description = "게시일", example = "2025-01-10T09:00:00Z")
        val publishedAt: String,
        @field:Schema(description = "YouTube 영상 URL", example = "https://www.youtube.com/watch?v=2oLfUjAqEcM")
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

@Schema(description = "탐색 채널 목록 응답")
data class DiscoverChannelResponses(
    @field:Schema(description = "채널 목록")
    val channels: List<DiscoverChannelResponse>,
) {
    companion object {
        fun from(details: List<YouTubeChannelDetail>): DiscoverChannelResponses =
            DiscoverChannelResponses(
                channels = details.map { DiscoverChannelResponse.from(it) },
            )
    }
}
