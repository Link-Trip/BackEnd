package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.AuthRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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

            **멱등성:**
            - `GET`을 제외한 모든 API는 `Idempotency-Key` 헤더가 필수입니다.
            - 동일한 `Idempotency-Key`로 처리 중인 요청이 있으면 409를 반환합니다.
        """,
        parameters = [
            Parameter(
                name = "Idempotency-Key",
                description = "멱등성 키 (UUID v4 권장, non-GET 요청 필수)",
                `in` = ParameterIn.HEADER,
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
            ),
        ],
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
                description = "잘못된 요청 (serialNumber 누락 또는 Idempotency-Key 헤더 누락)",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "멱등성 키 누락",
                                value =
                                    """{"code":"BAD_REQUEST_MISSING_IDEMPOTENCY_KEY",""" +
                                        """"message":"Idempotency-Key 헤더는 필수입니다."}""",
                            ),
                            ExampleObject(
                                name = "validation 실패",
                                value =
                                    """{"code":"BAD_REQUEST_VALIDATION",""" +
                                        """"message":"요청 값이 올바르지 않습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "동일한 멱등성 키의 요청이 이미 처리 중인 경우",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"DUPLICATE_REQUEST",""" +
                                        """"message":"이미 요청한 값입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun login(request: AuthRequest): ApiResponse<AuthResponse>
}
