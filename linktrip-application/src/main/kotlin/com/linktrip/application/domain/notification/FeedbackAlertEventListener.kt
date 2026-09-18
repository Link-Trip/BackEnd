package com.linktrip.application.domain.notification

import com.linktrip.application.port.output.notification.NotificationPort
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

@Component
class FeedbackAlertEventListener(
    private val notificationPorts: List<NotificationPort>,
) {
    @Async("NotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: FeedbackAlertEvent) {
        notificationPorts.forEach { port ->
            try {
                port.sendFeedbackAlert(event)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.warn(e) { "의견 알림 전송 인터럽트 (port=${port::class.simpleName})" }
                return
            } catch (e: Exception) {
                logger.warn(e) { "의견 알림 전송 실패 (port=${port::class.simpleName})" }
            }
        }
    }
}
