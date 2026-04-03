package com.linktrip.input.http.filter

import com.linktrip.application.domain.log.AccessLog
import com.linktrip.application.domain.log.AccessLogEvent
import com.linktrip.common.config.event.Events
import com.linktrip.common.logging.MdcKey
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.slf4j.MDC
import java.util.UUID

private val logger = KotlinLogging.logger {}

class MdcLoggingFilter : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val uri = httpRequest.requestURI
        val skipLogging = LOGGING_SKIP.any { uri.startsWith(it) }
        val startTime = System.currentTimeMillis()

        try {
            MDC.put(MdcKey.REQUEST_ID, UUID.randomUUID().toString().substring(0, 8))
            MDC.put(MdcKey.REQUEST_METHOD, httpRequest.method)
            MDC.put(MdcKey.REQUEST_URI, uri)
            MDC.put(MdcKey.CLIENT_IP, getClientIp(httpRequest))

            if (!skipLogging) {
                logger.info { ">>> ${httpRequest.method} $uri" }
            }

            chain.doFilter(request, response)

            val duration = System.currentTimeMillis() - startTime
            if (!skipLogging) {
                logger.info { "<<< ${httpRequest.method} $uri ${httpResponse.status} (${duration}ms)" }
                raiseAccessLogEvent(httpResponse.status, duration)
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error(e) { "!!! ${httpRequest.method} $uri 에러 (${duration}ms) - ${e.message}" }

            raiseAccessLogEvent(500, duration, e.message)
            throw e
        } finally {
            MDC.clear()
        }
    }

    private fun raiseAccessLogEvent(
        statusCode: Int,
        durationMs: Long,
        errorMessage: String? = null,
    ) {
        Events.raise(
            AccessLogEvent(
                accessLog =
                    AccessLog(
                        requestId = MDC.get(MdcKey.REQUEST_ID) ?: "unknown",
                        method = MDC.get(MdcKey.REQUEST_METHOD) ?: "unknown",
                        uri = MDC.get(MdcKey.REQUEST_URI) ?: "unknown",
                        clientIp = MDC.get(MdcKey.CLIENT_IP) ?: "unknown",
                        statusCode = statusCode,
                        durationMs = durationMs,
                        errorMessage = errorMessage,
                    ),
            ),
        )
    }

    private fun getClientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr

    companion object {
        private val LOGGING_SKIP =
            listOf(
                "/api/health/",
            )
    }
}
