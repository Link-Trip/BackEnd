package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.notification.ExceptionAlertEvent
import com.linktrip.application.port.output.notification.NotificationPort
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * NotificationPort의 No-Op 구현체.
 * 추후 Discord 웹훅 어댑터가 추가되면 이 클래스를 제거한다.
 */
@Component
class NoOpNotificationAdapter : NotificationPort {
    override fun sendExceptionAlert(event: ExceptionAlertEvent) {
        // ToDo 추후 Discord 웹훅 모듈로 옮긴 후, 실제 전송 구현
        logger.debug {
            "ExceptionAlert NoOp " +
                "(statusCode=${event.statusCode}, " +
                "message=${event.message})"
        }
    }
}
