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
        // ToDo 추후 ES 모듈로 옮긴 후, save 구현
        logger.debug {
            "(requestId=${accessLog.requestId}, " +
                "${accessLog.method} ${accessLog.uri} " +
                "${accessLog.statusCode} ${accessLog.durationMs}ms)"
        }
    }
}
