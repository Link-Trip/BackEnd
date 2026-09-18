package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "의견 전송 요청")
data class CreateFeedbackRequest(
    @field:Schema(
        description = "의견 유형 (SUGGESTION, BUG, ETC — 미선택 시 ETC)",
        example = "SUGGESTION",
        allowableValues = ["SUGGESTION", "BUG", "ETC"],
    )
    @field:NotBlank(message = "의견 유형은 필수입니다.")
    val type: String,
    @field:Schema(description = "의견 본문 (trim 후 1자 이상 200자 이하)", example = "지도에서 일정 순서를 바꿀 수 있으면 좋겠어요.")
    @field:NotBlank(message = "내용은 필수입니다.")
    @field:Size(max = 200, message = "내용은 200자 이하여야 합니다.")
    val content: String,
    @field:Schema(description = "앱 버전", example = "1.0")
    @field:NotBlank(message = "앱 버전은 필수입니다.")
    @field:Size(max = 20, message = "앱 버전은 20자 이하여야 합니다.")
    val appVersion: String,
    @field:Schema(description = "플랫폼 (IOS 또는 ANDROID)", example = "IOS", allowableValues = ["IOS", "ANDROID"])
    @field:NotBlank(message = "플랫폼은 필수입니다.")
    val platform: String,
    @field:Schema(description = "OS 버전", example = "15")
    @field:NotBlank(message = "OS 버전은 필수입니다.")
    @field:Size(max = 20, message = "OS 버전은 20자 이하여야 합니다.")
    val osVersion: String,
    @field:Schema(description = "기기 모델", example = "Pixel 8")
    @field:NotBlank(message = "기기 모델은 필수입니다.")
    @field:Size(max = 50, message = "기기 모델은 50자 이하여야 합니다.")
    val deviceModel: String,
)
