package com.linktrip.input.http.filter

import org.springframework.core.Ordered

object FilterOrder {
    const val MDC_LOGGING = Ordered.HIGHEST_PRECEDENCE
    const val JWT_AUTHENTICATION = Ordered.HIGHEST_PRECEDENCE + 1
}
