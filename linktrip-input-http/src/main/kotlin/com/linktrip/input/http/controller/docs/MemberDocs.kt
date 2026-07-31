package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.FcmTokenRequest
import com.linktrip.input.http.controller.dto.request.NotificationSettingRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.NotificationSettingResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Member", description = "회원 설정 API")
interface MemberDocs {
    @Operation(
        summary = "FCM 토큰 등록/갱신",
        description = """
            로그인한 회원의 FCM 디바이스 토큰과 플랫폼(IOS/ANDROID)을 등록합니다.
            이미 토큰이 있으면 새 값으로 갱신합니다 (앱 실행/토큰 리프레시 시 호출).

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
                description = "등록/갱신 성공",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (fcmToken/platform 누락 또는 지원하지 않는 플랫폼)",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "지원하지 않는 플랫폼",
                                value = """{"code":"BAD_REQUEST_PLATFORM","message":"지원하지 않는 플랫폼입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "회원을 찾을 수 없음",
            ),
        ],
    )
    fun registerFcmToken(
        memberId: String,
        request: FcmTokenRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "알림 수신 설정 변경 (ON/OFF)",
        description = """
            로그인한 회원의 푸시 알림 수신 여부를 변경합니다.

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
                description = "변경 성공 (변경 후 설정값 반환)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (enabled 누락)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "회원을 찾을 수 없음",
            ),
        ],
    )
    fun updateNotificationSetting(
        memberId: String,
        request: NotificationSettingRequest,
    ): ApiResponse<NotificationSettingResponse>
}
