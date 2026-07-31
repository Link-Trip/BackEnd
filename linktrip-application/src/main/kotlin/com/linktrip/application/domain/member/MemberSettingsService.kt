package com.linktrip.application.domain.member

import com.linktrip.application.port.input.MemberSettingsUseCase
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberSettingsService(
    private val memberPort: MemberPort,
) : MemberSettingsUseCase {
    @Transactional
    override fun registerFcmToken(
        memberId: String,
        fcmToken: String,
        platform: Platform,
    ) {
        val member = findMember(memberId)
        memberPort.update(member.registerFcmToken(fcmToken, platform))
    }

    @Transactional
    override fun updateNotificationEnabled(
        memberId: String,
        enabled: Boolean,
    ): Boolean {
        val member = findMember(memberId)
        return memberPort.update(member.updateNotificationEnabled(enabled)).notificationEnabled
    }

    private fun findMember(memberId: String): Member =
        memberPort.findById(memberId)
            ?: throw LinktripException(ExceptionCode.NOT_FOUND_MEMBER)
}
