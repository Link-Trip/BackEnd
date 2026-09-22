package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(description = "약관 동의 요청")
data class AgreeTermsRequest(
    @field:Schema(
        description = "동의할 약관 유형 목록 (필수 약관은 모두 포함해야 함)",
        example = "[\"SERVICE\", \"PRIVACY\"]",
    )
    @field:NotEmpty(message = "동의할 약관 목록은 필수입니다.")
    val types: List<String>,
)
