package com.linktrip.application.port.input

import com.linktrip.application.domain.member.Platform

interface MemberSettingsUseCase {
    fun registerFcmToken(
        memberId: String,
        fcmToken: String,
        platform: Platform,
    )

    fun getNotificationEnabled(memberId: String): Boolean

    fun updateNotificationEnabled(
        memberId: String,
        enabled: Boolean,
    ): Boolean
}
