package com.linktrip.application.port.output.external

import com.linktrip.application.domain.video.VideoAnalysisResult

interface VideoAnalyzePort {
    fun analyze(youtubeUrl: String): VideoAnalysisResult
}
