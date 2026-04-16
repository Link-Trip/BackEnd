package com.linktrip.application.port.output.external

import com.linktrip.application.domain.video.VideoAnalysisResult

interface VideoAnalyzePort {
    /**
     * YouTube 영상에서 자막을 추출한다.
     * 실패 유형에 따라 BAD_GATEWAY_YOUTUBE(Rate Limit) 또는 BAD_REQUEST_VIDEO(자막 없음) 예외를 던진다.
     */
    fun extractTranscript(youtubeUrl: String): String

    /**
     * 이미 추출된 자막을 Gemini로 분석한다.
     * 실패 시 BAD_GATEWAY_GEMINI 예외를 던진다.
     */
    fun analyzeFromTranscript(
        transcript: String,
        youtubeUrl: String,
    ): VideoAnalysisResult
}
