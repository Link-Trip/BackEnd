package com.linktrip.input.http.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class AuthRequest(
    @field:NotBlank(message = "시리얼 넘버는 필수입니다.")
    val serialNumber: String,
)
