package com.linktrip.application.domain.health

import com.linktrip.application.port.input.HealthCheckUseCase
import com.linktrip.application.port.output.persistence.DatabaseHealthCheckPort
import org.springframework.stereotype.Service

@Service
class HealthCheckService(
    private val databaseHealthCheckPort: DatabaseHealthCheckPort,
) : HealthCheckUseCase {
    override fun checkApiHealth(): Boolean = true

    override fun checkDatabaseHealth(): Boolean = databaseHealthCheckPort.isHealthy()
}
