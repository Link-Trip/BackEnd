package com.linktrip.application.domain.member

import com.linktrip.application.port.input.AuthUseCase
import com.linktrip.application.port.input.AuthUseCase.AuthResult
import com.linktrip.application.port.output.auth.OAuthPort
import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val oAuthPorts: List<OAuthPort>,
    private val memberService: MemberService,
    private val tokenProvider: TokenProvider,
) : AuthUseCase {
    override fun socialLogin(
        providerType: ProviderType,
        accessToken: String,
    ): AuthResult {
        val oAuthPort = getOAuthPort(providerType)
        val oAuthInfo = oAuthPort.requestUserInfo(accessToken)
        logger.info { "OAuth 사용자 정보 조회 완료: provider=$providerType, providerId=${oAuthInfo.providerId}" }

        val result = memberService.findOrCreateByOAuth(providerType, oAuthInfo)
        val token = tokenProvider.create(result.member.id)

        return AuthResult(
            memberId = result.member.id,
            accessToken = token,
            isNewMember = result.isNewMember,
        )
    }

    private fun getOAuthPort(providerType: ProviderType): OAuthPort =
        oAuthPorts.find { it.getProviderType() == providerType }
            ?: throw LinktripException(ExceptionCode.UNSUPPORTED_OAUTH_PROVIDER, providerType.name)
}
