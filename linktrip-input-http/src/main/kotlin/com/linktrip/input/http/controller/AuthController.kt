package com.linktrip.input.http.controller

import com.linktrip.application.port.input.AuthUseCase
import com.linktrip.input.http.controller.dto.request.OAuthLoginRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.OAuthLoginResponse
import org.springframework.validation.annotation.Validated
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
        @Validated @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> {
        val result =
            authUseCase.socialLogin(
                providerType = request.providerType,
                accessToken = request.accessToken,
            )

        val response =
            OAuthLoginResponse(
                memberId = result.memberId,
                accessToken = result.accessToken,
                isNewMember = result.isNewMember,
            )

        return if (result.isNewMember) {
            ApiResponse.created(response)
        } else {
            ApiResponse.ok(response)
        }
    }
}
