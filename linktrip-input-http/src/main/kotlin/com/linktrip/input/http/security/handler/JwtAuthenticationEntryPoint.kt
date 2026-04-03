package com.linktrip.input.http.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.input.http.controller.dto.response.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val exceptionCode = ExceptionCode.UNAUTHORIZED_AUTHENTICATION_FAILED

        response.status = exceptionCode.statusCode
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.writer.write(
            objectMapper.writeValueAsString(
                ExceptionResponse(
                    code = exceptionCode.name,
                    message = exceptionCode.defaultMessage,
                    cause = null,
                    timestamp = System.currentTimeMillis(),
                ),
            ),
        )
    }
}
