package com.linktrip.application.domain.log

/**
 * Access 로그 이벤트.
 * AOP에서 발행하고, EventListener에서 비동기로 처리한다.
 */
data class AccessLogEvent(
    val accessLog: AccessLog,
)
