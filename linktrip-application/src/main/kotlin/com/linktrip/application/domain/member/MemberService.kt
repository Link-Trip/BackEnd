package com.linktrip.application.domain.member

import com.linktrip.application.port.output.auth.OAuthInfo
import com.linktrip.application.port.output.persistence.MemberPort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class MemberService(
    private val memberPort: MemberPort,
) {
    @Transactional
    fun findOrCreateByOAuth(
        providerType: ProviderType,
        oAuthInfo: OAuthInfo,
    ): MemberLoginResult {
        memberPort.findByProviderTypeAndProviderId(providerType, oAuthInfo.providerId)
            ?.let { return MemberLoginResult(member = it, isNewMember = false) }

        val newMember =
            memberPort.save(
                Member.create(
                    email = oAuthInfo.email,
                    providerType = providerType,
                    providerId = oAuthInfo.providerId,
                ),
            )

        logger.info { "신규 회원 생성: memberId=${newMember.id}, provider=$providerType" }
        return MemberLoginResult(member = newMember, isNewMember = true)
    }

    data class MemberLoginResult(
        val member: Member,
        val isNewMember: Boolean,
    )
}
