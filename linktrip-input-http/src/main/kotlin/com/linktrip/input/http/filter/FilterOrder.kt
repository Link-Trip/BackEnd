package com.linktrip.input.http.filter

import org.springframework.core.Ordered

object FilterOrder {
    const val MDC_LOGGING = Ordered.HIGHEST_PRECEDENCE
}
