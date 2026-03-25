package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.member.Member
import com.linktrip.application.domain.member.ProviderType
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.output.persistence.mysql.entity.MemberEntity
import com.linktrip.output.persistence.mysql.repository.MemberJpaRepository
import org.springframework.stereotype.Component

@Component
class MemberAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberPort {
    override fun findByProviderTypeAndProviderId(
        providerType: ProviderType,
        providerId: String,
    ): Member? = memberJpaRepository.findByProviderTypeAndProviderId(providerType, providerId)?.toDomain()

    override fun findByEmail(email: String): Member? = memberJpaRepository.findByEmail(email)?.toDomain()

    override fun save(member: Member): Member = memberJpaRepository.save(MemberEntity.from(member)).toDomain()
}
