package com.linktrip.application.port.input

import com.linktrip.application.domain.video.VideoAnalysisResult

interface VideoAnalyzeUseCase {
    fun analyzeVideo(youtubeUrl: String): VideoAnalysisResult
}
