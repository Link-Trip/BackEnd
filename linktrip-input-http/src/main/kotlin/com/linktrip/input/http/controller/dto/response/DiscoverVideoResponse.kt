package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "탐색 영상 정보")
data class DiscoverVideoResponse(
    @field:Schema(description = "YouTube 영상 ID", example = "2oLfUjAqEcM")
    val videoId: String,
    @field:Schema(description = "YouTube 영상 URL", example = "https://www.youtube.com/watch?v=2oLfUjAqEcM")
    val videoUrl: String,
    @field:Schema(description = "영상 제목", example = "후쿠오카 1박2일 맛집 여행")
    val title: String,
    @field:Schema(description = "영상 설명")
    val description: String,
    @field:Schema(description = "썸네일 URL", example = "https://i.ytimg.com/vi/2oLfUjAqEcM/maxresdefault.jpg")
    val thumbnailUrl: String,
    @field:Schema(description = "채널 ID", example = "UCxxxxxxxxxxxxxxxxxxxxxx")
    val channelId: String,
    @field:Schema(description = "채널 이름", example = "여행 유튜버")
    val channelTitle: String,
    @field:Schema(description = "조회수", example = "150000")
    val viewCount: Long,
    @field:Schema(description = "좋아요 수", example = "3200")
    val likeCount: Long,
    @field:Schema(description = "영상 길이 (ISO 8601 형식)", example = "PT35M29S")
    val duration: String,
    @field:Schema(description = "게시일 (ISO 8601 형식)", example = "2025-01-10T09:00:00Z")
    val publishedAt: String,
    @field:Schema(description = "지역", example = "동아시아")
    val region: String,
    @field:Schema(description = "국가", example = "일본")
    val country: String,
    @field:Schema(description = "도시", example = "후쿠오카", nullable = true)
    val city: String?,
    @field:Schema(description = "테마", example = "맛집여행", nullable = true)
    val theme: String?,
) {
    companion object {
        fun from(detail: YouTubeVideoMeta): DiscoverVideoResponse =
            DiscoverVideoResponse(
                videoId = detail.videoId,
                videoUrl = VideoAnalysisTask.buildUrl(detail.videoId),
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

@Schema(description = "탐색 영상 목록 응답")
data class DiscoverVideoResponses(
    @field:Schema(description = "영상 목록")
    val videos: List<DiscoverVideoResponse>,
) {
    companion object {
        fun from(details: List<YouTubeVideoMeta>): DiscoverVideoResponses =
            DiscoverVideoResponses(
                videos = details.map { DiscoverVideoResponse.from(it) },
            )
    }
}

@Schema(description = "테마별 영상 커서 페이지네이션 응답")
data class DiscoverVideoCursorResponse(
    @field:Schema(description = "영상 목록")
    val videos: List<DiscoverVideoResponse>,
    @field:Schema(description = "다음 페이지 커서 (마지막 페이지면 null)", nullable = true)
    val nextCursor: String?,
    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
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
