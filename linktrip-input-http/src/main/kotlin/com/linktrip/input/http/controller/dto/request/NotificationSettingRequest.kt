package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "알림 설정 변경 요청")
data class NotificationSettingRequest(
    @field:Schema(description = "알림 수신 여부", example = "true")
    @field:NotNull(message = "알림 수신 여부는 필수입니다.")
    val enabled: Boolean?,
)
