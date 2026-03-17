package com.linktrip.application.domain.youtube

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val publishedAt: String,
)
