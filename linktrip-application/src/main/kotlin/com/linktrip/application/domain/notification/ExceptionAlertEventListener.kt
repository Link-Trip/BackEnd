package com.linktrip.application.domain.notification

import com.linktrip.application.port.output.notification.NotificationPort
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ExceptionAlertEventListener(
    private val notificationPorts: List<NotificationPort>,
) {
    @Async("NotificationExecutor")
    @EventListener
    fun handle(event: ExceptionAlertEvent) {
        notificationPorts.forEach { port ->
            try {
                port.sendExceptionAlert(event)
            } catch (e: Exception) {
                logger.warn(e) {
                    "알림 전송 실패 (port=${port::class.simpleName}, " +
                        "statusCode=${event.statusCode})"
                }
            }
        }
    }
}
