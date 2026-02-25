package com.linktrip.application.port.output

interface DatabaseHealthCheckPort {
    fun isHealthy(): Boolean
}
