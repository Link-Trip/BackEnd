package com.linktrip.output.http.adapter

import com.linktrip.application.domain.notification.ExceptionAlertEvent
import com.linktrip.application.port.output.notification.NotificationPort
import com.linktrip.output.http.properties.DiscordNotificationProperties
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

@Component
class DiscordNotificationAdapter(
    private val properties: DiscordNotificationProperties,
    private val discordRestClient: RestClient,
) : NotificationPort {
    override fun sendExceptionAlert(event: ExceptionAlertEvent) {
        val message = buildMessage(event)
        val payload = mapOf("content" to message)
        runCatching {
            discordRestClient.post()
                .uri(properties.webhookUrlError)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        }.onFailure { e ->
            logger.warn(e) {
                "디스코드 웹훅 전송 실패 " +
                    "(statusCode=${event.statusCode}, message=${event.message})"
            }
        }
    }

    private fun buildMessage(event: ExceptionAlertEvent): String {
        val fullMessage =
            """
            🚨 <@${properties.mentionUserId}>

            ❗ [Linktrip 예외 알림]
            🔢 상태코드: ${event.statusCode}
            📝 메시지: ${event.message}
            💥 원인: ${event.cause ?: "없음"}
            ⏰ 발생시각: ${event.timestamp}
            🧵 스택트레이스:
            ${event.stackTrace ?: "없음"}
            """.trimIndent()

        val limit = 1000
        return if (fullMessage.length <= limit) {
            fullMessage
        } else {
            fullMessage.take(limit) + "\n\n...(truncated)"
        }
    }
}
