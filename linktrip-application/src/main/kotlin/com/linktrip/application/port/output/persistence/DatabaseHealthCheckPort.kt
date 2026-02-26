package com.linktrip.application.port.output.persistence

interface DatabaseHealthCheckPort {
    fun isHealthy(): Boolean
}
