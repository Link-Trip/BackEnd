package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.port.input.KeywordAnalyzeResult

data class KeywordAnalyzeResponse(
    val keywordCount: Int,
    val analyzedCount: Int,
    val tasks: List<AnalyzedTaskResponse>,
) {
    companion object {
        fun from(result: KeywordAnalyzeResult): KeywordAnalyzeResponse =
            KeywordAnalyzeResponse(
                keywordCount = result.keywordCount,
                analyzedCount = result.tasks.size,
                tasks = result.tasks.map { AnalyzedTaskResponse.from(it) },
            )
    }
}

data class AnalyzedTaskResponse(
    val id: String,
    val youtubeUrl: String,
    val status: String,
) {
    companion object {
        fun from(task: com.linktrip.application.domain.video.VideoAnalysisTask): AnalyzedTaskResponse =
            AnalyzedTaskResponse(
                id = task.id,
                youtubeUrl = task.youtubeUrl,
                status = task.status.name,
            )
    }
}
