package com.linktrip.application.port.input

import com.linktrip.application.domain.video.VideoAnalysisTask

interface KeywordAnalyzeUseCase {
    fun analyzeByKeywords(
        region: String?,
        country: String?,
        maxResults: Int,
    ): KeywordAnalyzeResult
}

data class KeywordAnalyzeResult(
    val keywordCount: Int,
    val tasks: List<VideoAnalysisTask>,
)
