package com.linktrip.application.port.output.auth

import com.linktrip.application.domain.member.ProviderType

data class OAuthInfo(
    val providerType: ProviderType,
    val providerId: String,
    val email: String?,
)
