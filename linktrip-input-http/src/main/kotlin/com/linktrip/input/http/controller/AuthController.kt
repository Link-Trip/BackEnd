package com.linktrip.input.http.controller

import com.linktrip.application.port.input.AuthUseCase
import com.linktrip.input.http.controller.dto.request.AuthRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.AuthResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authUseCase: AuthUseCase,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AuthRequest,
    ): ApiResponse<AuthResponse> {
        val result = authUseCase.authenticateBySerial(request.serialNumber)

        val response =
            AuthResponse(
                memberId = result.memberId,
                accessToken = result.accessToken,
            )

        return if (result.isNewMember) {
            ApiResponse.created(response)
        } else {
            ApiResponse.ok(response)
        }
    }
}
