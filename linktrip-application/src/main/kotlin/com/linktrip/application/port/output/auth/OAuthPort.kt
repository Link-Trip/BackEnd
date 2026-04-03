package com.linktrip.application.port.output.auth

import com.linktrip.application.domain.member.ProviderType

interface OAuthPort {
    fun getProviderType(): ProviderType

    fun requestUserInfo(accessToken: String): OAuthInfo
}
