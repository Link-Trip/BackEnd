package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.member.ProviderType
import com.linktrip.output.persistence.mysql.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberEntity, String> {
    fun findByProviderTypeAndProviderId(
        providerType: ProviderType,
        providerId: String,
    ): MemberEntity?
}
