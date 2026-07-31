package com.linktrip.input.http.controller

import com.linktrip.application.domain.member.Platform
import com.linktrip.application.port.input.MemberSettingsUseCase
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.controller.docs.MemberDocs
import com.linktrip.input.http.controller.dto.request.FcmTokenRequest
import com.linktrip.input.http.controller.dto.request.NotificationSettingRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.NotificationSettingResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members/me")
class MemberController(
    private val memberSettingsUseCase: MemberSettingsUseCase,
) : MemberDocs {
    @PutMapping("/fcm-token")
    override fun registerFcmToken(
        @AuthenticatedMember memberId: String,
        @Validated @RequestBody request: FcmTokenRequest,
    ): ApiResponse<Unit> {
        memberSettingsUseCase.registerFcmToken(
            memberId = memberId,
            fcmToken = request.fcmToken,
            platform = Platform.from(request.platform),
        )
        return ApiResponse.ok()
    }

    @PutMapping("/notification")
    override fun updateNotificationSetting(
        @AuthenticatedMember memberId: String,
        @Validated @RequestBody request: NotificationSettingRequest,
    ): ApiResponse<NotificationSettingResponse> {
        val enabled =
            memberSettingsUseCase.updateNotificationEnabled(
                memberId = memberId,
                enabled = requireNotNull(request.enabled),
            )
        return ApiResponse.ok(NotificationSettingResponse(enabled = enabled))
    }
}
