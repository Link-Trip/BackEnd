package com.linktrip.application.domain.youtube

import java.time.LocalDateTime

data class YouTubeChannelDetail(
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val recentVideos: List<YouTubeRecentVideo> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
