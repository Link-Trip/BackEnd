package com.linktrip.application.domain.video

data class VideoAnalyzeEvent(
    val videoAnalysisTaskId: String,
    val youtubeUrl: String,
    val source: Source,
)
