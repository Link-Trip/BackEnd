package com.linktrip.application.domain.log

import com.linktrip.application.port.output.log.AccessLogPort
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class AccessLogEventListener(
    private val accessLogPorts: List<AccessLogPort>,
) {
    @Async("AccessLogExecutor")
    @EventListener
    fun handle(event: AccessLogEvent) {
        accessLogPorts.forEach { port ->
            try {
                port.save(event.accessLog)
            } catch (e: Exception) {
                logger.warn(e) { "AccessLog 저장 실패 (port=${port::class.simpleName}, requestId=${event.accessLog.requestId})" }
            }
        }
    }
}
