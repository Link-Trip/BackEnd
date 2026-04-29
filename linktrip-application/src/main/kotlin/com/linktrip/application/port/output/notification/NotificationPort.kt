package com.linktrip.application.port.output.notification

import com.linktrip.application.domain.notification.CostAlertEvent
import com.linktrip.application.domain.notification.ExceptionAlertEvent

/**
 * 알림 전송을 위한 Output Port.
 * 추후 Discord 웹훅 어댑터에서 구현체를 제공한다.
 */
interface NotificationPort {
    fun sendExceptionAlert(event: ExceptionAlertEvent)

    fun sendCostAlert(event: CostAlertEvent)
}
