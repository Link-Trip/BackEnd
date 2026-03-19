package com.linktrip.application.domain.log

import com.linktrip.application.port.output.log.AccessLogPort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AccessLogEventListenerTest {
    @Test
    fun `AccessLog 이벤트가 발생하면_등록된 모든 AccessLogPort에 로그를 저장한다`() {
        // given - 2개의 AccessLogPort가 등록된 상태
        val port1 = mock<AccessLogPort>()
        val port2 = mock<AccessLogPort>()
        val listener = AccessLogEventListener(listOf(port1, port2))

        val accessLog = AccessLog(
            requestId = "req-1",
            method = "GET",
            uri = "/api/test",
            clientIp = "127.0.0.1",
            statusCode = 200,
            durationMs = 50,
        )
        val event = AccessLogEvent(accessLog)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - 모든 port에 로그가 저장된다
        verify(port1).save(accessLog)
        verify(port2).save(accessLog)
    }

    @Test
    fun `하나의 AccessLogPort에서 저장 실패해도_나머지 port에는 정상적으로 저장한다`() {
        // given - port1에서 저장 시 예외가 발생하는 상태
        val port1 = mock<AccessLogPort>()
        val port2 = mock<AccessLogPort>()
        val listener = AccessLogEventListener(listOf(port1, port2))

        val accessLog = AccessLog(
            requestId = "req-1",
            method = "POST",
            uri = "/api/data",
            clientIp = "10.0.0.1",
            statusCode = 500,
            durationMs = 100,
        )
        val event = AccessLogEvent(accessLog)

        whenever(port1.save(accessLog)).thenThrow(RuntimeException("저장 실패"))

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - port1이 실패해도 port2에는 정상적으로 저장된다
        verify(port1).save(accessLog)
        verify(port2).save(accessLog)
    }
}
