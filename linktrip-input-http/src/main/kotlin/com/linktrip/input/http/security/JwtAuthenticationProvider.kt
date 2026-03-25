package com.linktrip.input.http.security

import com.linktrip.application.port.output.auth.TokenProvider
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationProvider(
    private val tokenProvider: TokenProvider,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val jwt = authentication.credentials as String

        try {
            if (!tokenProvider.validate(jwt)) {
                throw BadCredentialsException("유효하지 않은 JWT 토큰입니다.")
            }

            val memberId = tokenProvider.extractMemberId(jwt)
            return PostAuthorizationToken(memberId)
        } catch (e: BadCredentialsException) {
            throw e
        } catch (e: Exception) {
            throw BadCredentialsException("JWT 인증 처리 중 오류가 발생했습니다.", e)
        }
    }

    override fun supports(authentication: Class<*>): Boolean =
        PreAuthorizationToken::class.java.isAssignableFrom(authentication)
}
