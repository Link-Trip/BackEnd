package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.AgreeTermsRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.ExceptionResponse
import com.linktrip.input.http.controller.dto.response.TermsResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Terms", description = "약관 조회·동의 API")
interface TermsDocs {
    @Operation(
        summary = "약관 목록 조회 (동의 여부 포함)",
        description = """
            현재 노출 대상 약관 목록과 로그인한 회원의 동의 여부를 조회합니다.

            **클라이언트 사용법:**
            - 앱 시작(로그인) 후 이 API를 호출해 `agreed=false`인 약관이 하나라도 있으면 동의 바텀시트를 노출합니다.
            - `detailUrl`은 "상세보기"에서 웹뷰로 엽니다.
            - 약관이 개정되어 새 버전이 활성화되면 기존 동의자도 `agreed=false`로 내려오므로
              재동의 시트가 자동으로 노출됩니다.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/위조)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"UNAUTHORIZED_AUTHENTICATION_FAILED","message":"인증 정보가 없습니다.",""" +
                                        """"timestamp":1785390616431}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getTerms(
        @Parameter(hidden = true) memberId: String,
    ): ApiResponse<TermsResponses>

    @Operation(
        summary = "약관 동의",
        description = """
            로그인한 회원의 약관 동의를 기록합니다.

            - 필수 약관이 하나라도 빠지면 400 `BAD_REQUEST_TERMS_REQUIRED`를 반환합니다.
            - 이미 동의한 약관이 포함되어도 무시하고 200을 반환합니다 (멱등).
            - 동의 이력은 약관 버전 단위로 저장됩니다.

            **멱등성:** `Idempotency-Key` 헤더가 필수입니다.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
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
                description = "동의 성공 (data 없음)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                name = "필수 약관 누락",
                                value =
                                    """{"code":"BAD_REQUEST_TERMS_REQUIRED","message":"필수 약관에 모두 동의해야 합니다.",""" +
                                        """"timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "지원하지 않는 약관 유형",
                                value =
                                    """{"code":"BAD_REQUEST_TERMS_TYPE","message":"지원하지 않는 약관 유형입니다.",""" +
                                        """"timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "types 누락 (validation 실패)",
                                value =
                                    """{"code":"BAD_REQUEST_VALIDATION","message":"요청 형식이 잘못되었습니다.",""" +
                                        """"cause":"types: 동의할 약관 목록은 필수입니다.","timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "멱등성 키 누락",
                                value =
                                    """{"code":"BAD_REQUEST_MISSING_IDEMPOTENCY_KEY",""" +
                                        """"message":"Idempotency-Key 헤더는 필수입니다.","timestamp":1785390616431}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/위조)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"UNAUTHORIZED_AUTHENTICATION_FAILED","message":"인증 정보가 없습니다.",""" +
                                        """"timestamp":1785390616431}""",
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
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"DUPLICATE_REQUEST","message":"이미 요청한 값입니다.",""" +
                                        """"timestamp":1785390616431}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun agree(
        @Parameter(hidden = true) memberId: String,
        request: AgreeTermsRequest,
    ): ApiResponse<Unit>
}
