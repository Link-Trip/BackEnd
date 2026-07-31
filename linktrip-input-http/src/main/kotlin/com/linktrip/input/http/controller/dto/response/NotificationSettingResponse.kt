package com.linktrip.input.http.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 설정 응답")
data class NotificationSettingResponse(
    @field:Schema(description = "알림 수신 여부", example = "true")
    val enabled: Boolean,
)
