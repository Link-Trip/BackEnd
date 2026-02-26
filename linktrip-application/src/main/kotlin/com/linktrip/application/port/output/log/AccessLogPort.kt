package com.linktrip.application.port.output.log

import com.linktrip.application.domain.log.AccessLog

/**
 * Access 로그 저장을 위한 Output Port.
 * 추후 Elasticsearch 등의 어댑터에서 구현체를 제공한다.
 */
interface AccessLogPort {
    fun save(accessLog: AccessLog)
}
