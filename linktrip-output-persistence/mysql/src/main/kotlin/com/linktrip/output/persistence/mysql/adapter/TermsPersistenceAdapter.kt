package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.terms.Terms
import com.linktrip.application.domain.terms.TermsAgreement
import com.linktrip.application.port.output.persistence.TermsPersistencePort
import com.linktrip.output.persistence.mysql.entity.TermsAgreementEntity
import com.linktrip.output.persistence.mysql.repository.TermsAgreementJpaRepository
import com.linktrip.output.persistence.mysql.repository.TermsJpaRepository
import org.springframework.stereotype.Component

@Component
class TermsPersistenceAdapter(
    private val termsJpaRepository: TermsJpaRepository,
    private val termsAgreementJpaRepository: TermsAgreementJpaRepository,
) : TermsPersistencePort {
    override fun findAllActive(): List<Terms> =
        termsJpaRepository.findAllByActiveTrueAndDeletedFalseOrderByCreatedAtAsc().map { it.toDomain() }

    override fun findAgreedTermsIds(
        memberId: String,
        termsIds: List<String>,
    ): Set<String> {
        if (termsIds.isEmpty()) return emptySet()
        return termsAgreementJpaRepository
            .findAllByMemberIdAndTermsIdInAndDeletedFalse(memberId, termsIds)
            .map { it.termsId }
            .toSet()
    }

    override fun saveAgreements(agreements: List<TermsAgreement>) {
        termsAgreementJpaRepository.saveAll(agreements.map { TermsAgreementEntity.from(it) })
    }
}
