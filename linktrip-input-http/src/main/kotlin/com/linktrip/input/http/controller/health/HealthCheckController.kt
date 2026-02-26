package com.linktrip.input.http.controller.health

import com.linktrip.application.port.input.HealthCheckUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthCheckController(
    private val healthCheckUseCase: HealthCheckUseCase,
) {
    @GetMapping("/api")
    fun apiHealth(): ResponseEntity<HealthResponse> =
        ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                service = "linktrip-api",
            ),
        )

    @GetMapping("/db")
    fun dbHealth(): ResponseEntity<HealthResponse> {
        val isHealthy = healthCheckUseCase.checkDatabaseHealth()
        return if (isHealthy) {
            ResponseEntity.ok(
                HealthResponse(
                    status = "UP",
                    service = "linktrip-db",
                ),
            )
        } else {
            ResponseEntity.internalServerError().body(
                HealthResponse(
                    status = "DOWN",
                    service = "linktrip-db",
                ),
            )
        }
    }

    data class HealthResponse(
        val status: String,
        val service: String,
    )
}
