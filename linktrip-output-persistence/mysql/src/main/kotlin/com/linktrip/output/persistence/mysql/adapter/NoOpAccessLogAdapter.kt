package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.log.AccessLog
import com.linktrip.application.port.output.log.AccessLogPort
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * AccessLogPort의 No-Op 구현체.
 * 추후 Elasticsearch 어댑터가 추가되면 이 클래스를 제거한다.
 */
@Component
class NoOpAccessLogAdapter : AccessLogPort {
    override fun save(accessLog: AccessLog) {
        logger.debug {
            "AccessLog 저장 미구현 " +
                "(requestId=${accessLog.requestId}, " +
                "${accessLog.method} ${accessLog.uri} " +
                "${accessLog.statusCode} ${accessLog.durationMs}ms)"
        }
    }
}
