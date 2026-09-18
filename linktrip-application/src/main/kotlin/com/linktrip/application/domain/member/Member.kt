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

    /**
     * 탈퇴 처리: 개인정보(기기식별자)를 비가역 값으로 마스킹하고 기기 연결 정보를 제거한다.
     * 원본 serialNumber 와 무관한 값으로 대체되므로 복원·재식별이 불가능하다.
     */
    fun withdraw(): Member =
        copy(
            serialNumber = WITHDRAWN_SERIAL_PREFIX + id,
            fcmToken = null,
            platform = null,
        )

    val isWithdrawn: Boolean
        get() = serialNumber.startsWith(WITHDRAWN_SERIAL_PREFIX)

    companion object {
        const val WITHDRAWN_SERIAL_PREFIX = "DELETED_"

        fun create(serialNumber: String): Member =
            Member(
                id = IdGenerator.generate(),
                serialNumber = serialNumber,
            )
    }
}
