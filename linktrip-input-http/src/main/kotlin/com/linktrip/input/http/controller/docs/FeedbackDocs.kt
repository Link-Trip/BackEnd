package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.CreateFeedbackRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Feedback", description = "의견 전송 API")
interface FeedbackDocs {
    @Operation(
        summary = "의견 전송",
        description = """
            로그인한 회원이 앱 의견을 전송합니다. 하루(KST 기준) 최대 5회로 제한하며,
            초과 시 429 `FEEDBACK_DAILY_LIMIT_EXCEEDED`를 반환합니다.
            앱 버전·플랫폼·OS 버전·기기 모델은 클라이언트가 자동 첨부합니다.

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
                description = "전송 성공 (data 없음)",
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
                                name = "content 누락 (validation 실패)",
                                value =
                                    """{"code":"BAD_REQUEST_VALIDATION","message":"요청 형식이 잘못되었습니다.",""" +
                                        """"cause":"content: 내용은 필수입니다.","timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "지원하지 않는 의견 유형",
                                value =
                                    """{"code":"BAD_REQUEST_FEEDBACK_TYPE","message":"지원하지 않는 의견 유형입니다.",""" +
                                        """"timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "지원하지 않는 플랫폼",
                                value =
                                    """{"code":"BAD_REQUEST_PLATFORM","message":"지원하지 않는 플랫폼입니다.",""" +
                                        """"timestamp":1785390616431}""",
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
                                name = "토큰 없음",
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
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "429",
                description = "일일 전송 횟수(5회) 초과 또는 요청 횟수 초과",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ExceptionResponse::class),
                        examples = [
                            ExampleObject(
                                name = "일일 의견 횟수 초과",
                                value =
                                    """{"code":"FEEDBACK_DAILY_LIMIT_EXCEEDED",""" +
                                        """"message":"오늘 보낼 수 있는 의견 횟수를 모두 사용했습니다.","timestamp":1785390616431}""",
                            ),
                            ExampleObject(
                                name = "요청 횟수 초과 (rate limit)",
                                value =
                                    """{"code":"TOO_MANY_REQUESTS",""" +
                                        """"message":"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.","timestamp":1785390616431}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun createFeedback(
        @Parameter(hidden = true) memberId: String,
        request: CreateFeedbackRequest,
    ): ApiResponse<Unit>
}
