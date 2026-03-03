package com.linktrip.application.port.input

import com.linktrip.application.domain.video.VideoSummary

interface VideoAnalyzeUseCase {
    fun analyzeVideo(youtubeUrl: String): VideoSummary
}
