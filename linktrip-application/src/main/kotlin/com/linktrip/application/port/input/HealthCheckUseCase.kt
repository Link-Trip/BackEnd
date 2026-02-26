package com.linktrip.application.port.input

interface HealthCheckUseCase {
    fun checkApiHealth(): Boolean

    fun checkDatabaseHealth(): Boolean
}
