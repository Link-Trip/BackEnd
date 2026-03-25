package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.member.Member
import com.linktrip.application.domain.member.ProviderType

interface MemberPort {
    fun findByProviderTypeAndProviderId(
        providerType: ProviderType,
        providerId: String,
    ): Member?

    fun save(member: Member): Member
}
