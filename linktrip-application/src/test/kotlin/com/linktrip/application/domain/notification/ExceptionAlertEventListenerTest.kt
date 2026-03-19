package com.linktrip.application.domain.notification

import com.linktrip.application.port.output.notification.NotificationPort
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ExceptionAlertEventListenerTest {
    @Test
    fun `예외 알림 이벤트가 발생하면_등록된 모든 NotificationPort에 알림을 전송한다`() {
        // given - 2개의 NotificationPort가 등록된 상태
        val port1 = mock<NotificationPort>()
        val port2 = mock<NotificationPort>()
        val listener = ExceptionAlertEventListener(listOf(port1, port2))

        val event =
            ExceptionAlertEvent(
                message = "에러 발생",
                cause = "NullPointerException",
                statusCode = 500,
                stackTrace = "at com.linktrip...",
            )

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - 모든 port에 알림이 전송된다
        verify(port1).sendExceptionAlert(event)
        verify(port2).sendExceptionAlert(event)
    }

    @Test
    fun `첫 번째 port에서 InterruptedException 발생 시_이후 port 호출을 중단하고_스레드 인터럽트 플래그를 복원한다`() {
        // given - port1에서 InterruptedException이 발생하는 상태
        val port1 = mock<NotificationPort>()
        val port2 = mock<NotificationPort>()
        val listener = ExceptionAlertEventListener(listOf(port1, port2))

        val event =
            ExceptionAlertEvent(
                message = "에러",
                cause = null,
                statusCode = 500,
                stackTrace = null,
            )

        doAnswer { throw InterruptedException("중단") }.whenever(port1).sendExceptionAlert(event)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - port2는 호출되지 않는다 (return으로 즉시 중단)
        verify(port1).sendExceptionAlert(event)
        verify(port2, never()).sendExceptionAlert(event)

        // then - 스레드 인터럽트 플래그가 복원되어 있다
        //   프로덕션 코드에서 Thread.currentThread().interrupt()를 제거하면 이 검증이 실패한다
        assertTrue(Thread.currentThread().isInterrupted) {
            "InterruptedException 발생 후 Thread.interrupt()로 플래그가 복원되어야 한다"
        }

        // cleanup - 다른 테스트에 영향 주지 않도록 인터럽트 플래그 클리어
        Thread.interrupted()
    }

    @Test
    fun `두 번째 port에서 RuntimeException 발생 시_해당 port만 건너뛰고_나머지 port는 정상적으로 알림을 전송한다`() {
        // given - port2에서 RuntimeException이 발생하는 상태
        val port1 = mock<NotificationPort>()
        val port2 = mock<NotificationPort>()
        val port3 = mock<NotificationPort>()
        val listener = ExceptionAlertEventListener(listOf(port1, port2, port3))

        val event =
            ExceptionAlertEvent(
                message = "에러",
                cause = null,
                statusCode = 500,
                stackTrace = null,
            )

        whenever(port2.sendExceptionAlert(event)).thenThrow(RuntimeException("전송 실패"))

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - port2만 실패하고, port1과 port3은 정상적으로 알림이 전송된다
        //   RuntimeException은 catch 후 continue하므로 port3까지 도달한다
        verify(port1).sendExceptionAlert(event)
        verify(port2).sendExceptionAlert(event)
        verify(port3).sendExceptionAlert(event)
    }
}
