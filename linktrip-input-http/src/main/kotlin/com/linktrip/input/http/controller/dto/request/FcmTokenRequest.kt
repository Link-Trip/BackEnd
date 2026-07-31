package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "FCM 토큰 등록 요청")
data class FcmTokenRequest(
    @field:Schema(description = "FCM 디바이스 토큰", example = "dQw4w9WgXcQ:APA91bF...")
    @field:NotBlank(message = "FCM 토큰은 필수입니다.")
    val fcmToken: String,
    @field:Schema(description = "플랫폼 (IOS 또는 ANDROID)", example = "IOS", allowableValues = ["IOS", "ANDROID"])
    @field:NotBlank(message = "플랫폼은 필수입니다.")
    val platform: String,
)
