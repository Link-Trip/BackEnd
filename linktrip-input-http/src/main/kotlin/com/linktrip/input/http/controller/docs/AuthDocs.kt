package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.AuthRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Auth", description = "인증 API")
interface AuthDocs {
    @Operation(
        summary = "로그인 / 회원가입",
        description = """
            시리얼넘버로 회원가입(201) 또는 로그인(200) 통합 처리합니다.
            JWT Access Token을 발급합니다.
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "로그인 성공 (기존 회원)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "회원가입 성공 (신규 회원)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (serialNumber 누락)",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"BAD_REQUEST_VALIDATION",""" +
                                        """"message":"요청 값이 올바르지 않습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun login(request: AuthRequest): ApiResponse<AuthResponse>
}
