package com.linktrip.input.http.controller.health

import com.linktrip.application.port.input.HealthCheckUseCase
import com.linktrip.input.http.controller.dto.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthCheckController(
    private val healthCheckUseCase: HealthCheckUseCase,
) {
    @GetMapping("/api")
    fun apiHealth(): ApiResponse<HealthResponse> =
        ApiResponse.ok(
            HealthResponse(status = "UP", service = "linktrip-api"),
        )

    @GetMapping("/db")
    fun dbHealth(): ApiResponse<HealthResponse> {
        val isHealthy = healthCheckUseCase.checkDatabaseHealth()
        val status = if (isHealthy) "UP" else "DOWN"
        return ApiResponse.ok(
            HealthResponse(status = status, service = "linktrip-db"),
        )
    }

    data class HealthResponse(
        val status: String,
        val service: String,
    )
}
