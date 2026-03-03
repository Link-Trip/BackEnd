package com.linktrip.application.domain.video

data class VideoAnalyzeEvent(
    val videoSummaryId: String,
    val youtubeUrl: String,
)
