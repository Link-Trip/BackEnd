package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.member.Member
import com.linktrip.application.domain.member.ProviderType
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.output.persistence.mysql.entity.MemberEntity
import com.linktrip.output.persistence.mysql.repository.MemberJpaRepository
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class MemberAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberPort {
    override fun findByProviderTypeAndProviderId(
        providerType: ProviderType,
        providerId: String,
    ): Member? = memberJpaRepository.findByProviderTypeAndProviderId(providerType, providerId)?.toDomain()

    override fun save(member: Member): Member =
        try {
            memberJpaRepository.save(MemberEntity.from(member)).toDomain()
        } catch (_: DataIntegrityViolationException) {
            logger.warn { "동시 요청으로 인한 중복 회원 저장 시도 감지: provider=${member.providerType}" }
            memberJpaRepository.findByProviderTypeAndProviderId(member.providerType, member.providerId)!!
                .toDomain()
        }
}
