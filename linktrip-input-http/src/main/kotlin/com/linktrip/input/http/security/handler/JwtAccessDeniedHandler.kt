package com.linktrip.input.http.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.input.http.controller.dto.response.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val exceptionCode = ExceptionCode.FORBIDDEN_ACCESS_DENIED

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
