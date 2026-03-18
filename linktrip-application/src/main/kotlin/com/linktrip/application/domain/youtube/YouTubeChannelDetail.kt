package com.linktrip.application.domain.youtube

data class YouTubeChannelDetail(
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val recentVideos: List<YouTubeRecentVideo> = emptyList(),
)
