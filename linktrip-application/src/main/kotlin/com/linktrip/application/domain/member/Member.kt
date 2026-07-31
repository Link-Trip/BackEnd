package com.linktrip.application.domain.member

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class Member(
    val id: String,
    val serialNumber: String,
    val fcmToken: String? = null,
    val platform: Platform? = null,
    val notificationEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun registerFcmToken(
        fcmToken: String,
        platform: Platform,
    ): Member = copy(fcmToken = fcmToken, platform = platform)

    fun updateNotificationEnabled(enabled: Boolean): Member = copy(notificationEnabled = enabled)

    companion object {
        fun create(serialNumber: String): Member =
            Member(
                id = IdGenerator.generate(),
                serialNumber = serialNumber,
            )
    }
}
