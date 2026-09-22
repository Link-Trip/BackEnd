package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TermsAgreementEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TermsAgreementJpaRepository : JpaRepository<TermsAgreementEntity, String> {
    fun findAllByMemberIdAndTermsIdInAndDeletedFalse(
        memberId: String,
        termsIds: List<String>,
    ): List<TermsAgreementEntity>
}
