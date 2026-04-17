package com.linktrip.application.port.input

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalysisTask

interface VideoAnalyzeUseCase {
    fun analyzeVideo(
        youtubeUrl: String,
        source: Source,
    ): VideoAnalysisTask
}
