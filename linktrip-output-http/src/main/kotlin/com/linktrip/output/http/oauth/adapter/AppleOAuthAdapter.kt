package com.linktrip.output.http.oauth.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.linktrip.application.domain.member.ProviderType
import com.linktrip.application.port.output.auth.OAuthInfo
import com.linktrip.application.port.output.auth.OAuthPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.http.oauth.dto.ApplePublicKeyResponse
import io.jsonwebtoken.Jwts
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

private val logger = KotlinLogging.logger {}

@Component
class AppleOAuthAdapter(
    @param:Qualifier("appleOAuthRestClient") private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) : OAuthPort {
    override fun getProviderType(): ProviderType = ProviderType.APPLE

    override fun requestUserInfo(accessToken: String): OAuthInfo {
        val publicKeys = fetchApplePublicKeys()
        val claims = validateAndExtractClaims(accessToken, publicKeys)

        val providerId = claims.subject
        val email = claims["email"] as? String

        logger.debug { "Apple 사용자 정보 조회 성공: providerId=$providerId" }

        return OAuthInfo(
            providerType = ProviderType.APPLE,
            providerId = providerId,
            email = email,
        )
    }

    private fun fetchApplePublicKeys(): ApplePublicKeyResponse =
        restClient.get()
            .uri("/auth/keys")
            .retrieve()
            .body<ApplePublicKeyResponse>()
            ?: throw LinktripException(ExceptionCode.OAUTH_PROVIDER_ERROR)

    private fun validateAndExtractClaims(
        idToken: String,
        publicKeys: ApplePublicKeyResponse,
    ): io.jsonwebtoken.Claims {
        val headerPart = idToken.split(".").firstOrNull()
            ?: throw LinktripException(ExceptionCode.TOKEN_INVALID)

        val headerJson = String(Base64.getUrlDecoder().decode(headerPart))
        val headerMap = objectMapper.readValue(headerJson, Map::class.java)
        val kid = headerMap["kid"] as? String
            ?: throw LinktripException(ExceptionCode.TOKEN_INVALID)

        val matchingKey =
            publicKeys.keys.find { it.kid == kid }
                ?: throw LinktripException(ExceptionCode.TOKEN_INVALID)

        val publicKey = generatePublicKey(matchingKey)

        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(idToken)
            .payload
    }

    private fun generatePublicKey(key: ApplePublicKeyResponse.AppleKey): RSAPublicKey {
        val nBytes = Base64.getUrlDecoder().decode(key.n)
        val eBytes = Base64.getUrlDecoder().decode(key.e)
        val spec = RSAPublicKeySpec(BigInteger(1, nBytes), BigInteger(1, eBytes))
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec) as RSAPublicKey
    }
}
