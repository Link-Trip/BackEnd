package com.linktrip.application.port.input

import com.linktrip.application.domain.member.ProviderType

interface AuthUseCase {
    fun socialLogin(
        providerType: ProviderType,
        accessToken: String,
    ): AuthResult

    data class AuthResult(
        val memberId: String,
        val accessToken: String,
        val isNewMember: Boolean,
    )
}
