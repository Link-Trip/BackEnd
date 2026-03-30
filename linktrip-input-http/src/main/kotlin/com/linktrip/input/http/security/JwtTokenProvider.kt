package com.linktrip.input.http.security

import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret-key}") secretKeyString: String,
    @param:Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) : TokenProvider {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secretKeyString.toByteArray())

    override fun create(memberId: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(memberId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    override fun validate(token: String): Boolean =
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            logger.warn { "만료된 JWT 토큰: ${e.message}" }
            false
        } catch (e: JwtException) {
            logger.warn { "유효하지 않은 JWT 토큰: ${e.message}" }
            false
        } catch (e: IllegalArgumentException) {
            logger.warn { "잘못된 JWT 토큰: ${e.message}" }
            false
        }

    override fun extractMemberId(token: String): String =
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: ExpiredJwtException) {
            logger.warn { "만료된 JWT 토큰으로 memberId 추출 시도: ${e.message}" }
            throw LinktripException(ExceptionCode.UNAUTHORIZED_TOKEN_EXPIRED)
        } catch (e: JwtException) {
            logger.warn { "유효하지 않은 JWT 토큰으로 memberId 추출 시도: ${e.message}" }
            throw LinktripException(ExceptionCode.UNAUTHORIZED_TOKEN_INVALID)
        } catch (e: IllegalArgumentException) {
            logger.warn { "잘못된 JWT 토큰으로 memberId 추출 시도: ${e.message}" }
            throw LinktripException(ExceptionCode.UNAUTHORIZED_TOKEN_INVALID)
        }
}
