package com.linktrip.input.http.filter

import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.common.exception.ExceptionCode
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
) : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val uri = httpRequest.requestURI

        if (isWhitelisted(uri)) {
            chain.doFilter(request, response)
            return
        }

        val token = extractToken(httpRequest)
        if (token == null || !tokenProvider.validate(token)) {
            logger.warn { "인증 실패: uri=$uri" }
            httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            httpResponse.contentType = "application/json;charset=UTF-8"
            val body =
                """
                |{"message":"${ExceptionCode.UNAUTHORIZED.defaultMessage}",
                |"cause":null,"timestamp":${System.currentTimeMillis()}}
                """.trimMargin().replace("\n", "")
            httpResponse.writer.write(body)
            return
        }

        val memberId = tokenProvider.extractMemberId(token)
        httpRequest.setAttribute(MEMBER_ID_ATTRIBUTE, memberId)

        chain.doFilter(request, response)
    }

    private fun isWhitelisted(uri: String): Boolean = WHITELIST_PATHS.any { uri.startsWith(it) }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        return if (header.startsWith(BEARER_PREFIX)) {
            header.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    companion object {
        const val MEMBER_ID_ATTRIBUTE = "authenticatedMemberId"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "

        private val WHITELIST_PATHS =
            listOf(
                "/api/health",
                "/api/auth/login",
                "/api/swagger-ui",
                "/api/api-docs",
                "/api/v3/api-docs",
                "/actuator",
            )
    }
}
