package com.linktrip.input.http.controller.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExceptionResponse(
    @field:Schema(description = "에러 코드", example = "NOT_FOUND_VIDEO_ANALYSIS_TASK")
    val code: String?,
    @field:Schema(description = "에러 메시지", example = "영상 분석 결과를 찾을 수 없습니다.")
    val message: String?,
    @field:Schema(description = "에러 원인 상세", nullable = true)
    val cause: String?,
    @field:Schema(description = "에러 발생 시각 (Unix timestamp, ms)", example = "1711900000000")
    val timestamp: Long,
)
