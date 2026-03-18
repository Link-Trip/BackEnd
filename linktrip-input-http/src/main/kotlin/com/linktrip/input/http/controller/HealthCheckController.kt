package com.linktrip.input.http.controller

import com.linktrip.application.port.input.HealthCheckUseCase
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.controller.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthCheckController(
    private val healthCheckUseCase: HealthCheckUseCase,
) {
    @GetMapping("/api")
    fun apiHealth(): ApiResponse<HealthResponse> =
        ApiResponse.ok(
            HealthResponse(status = "UP", service = "linktrip-api"),
        )

    @GetMapping("/db")
    fun dbHealth(): ResponseEntity<ApiResponse<HealthResponse>> {
        val isHealthy = healthCheckUseCase.checkDatabaseHealth()
        val httpStatus = if (isHealthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE
        val healthStatus = if (isHealthy) "UP" else "DOWN"
        return ResponseEntity
            .status(httpStatus)
            .body(
                ApiResponse(
                    status = httpStatus.value(),
                    message = httpStatus.reasonPhrase,
                    data = HealthResponse(status = healthStatus, service = "linktrip-db"),
                ),
            )
    }

    @PostMapping("/error-test-global")
    fun errorTestGlobal() {
        throw RuntimeException()
    }

    @PostMapping("/error-test-linktrip")
    fun errorTestLinkTrip() {
        throw LinktripException(
            ExceptionCode.ERROR_TEST,
            "error-test: 의도적으로 발생시킨 LinktripException",
        )
    }

    data class HealthResponse(
        val status: String,
        val service: String,
    )
}
