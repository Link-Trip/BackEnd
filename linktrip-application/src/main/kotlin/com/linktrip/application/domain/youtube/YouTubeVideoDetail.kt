package com.linktrip.application.domain.youtube

data class YouTubeVideoDetail(
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
)
