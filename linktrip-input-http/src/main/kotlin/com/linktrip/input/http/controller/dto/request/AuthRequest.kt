package com.linktrip.input.http.controller.dto.request

import com.linktrip.application.domain.member.ProviderType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OAuthLoginRequest(
    @field:NotBlank(message = "액세스 토큰은 필수입니다.")
    val accessToken: String,
    @field:NotNull(message = "프로바이더 타입은 필수입니다.")
    val providerType: ProviderType,
)
