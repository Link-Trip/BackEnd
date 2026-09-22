package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.terms.TermsAgreement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "terms_agreement",
    indexes = [
        Index(name = "idx_terms_agreement_member", columnList = "member_id"),
    ],
)
class TermsAgreementEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "member_id", nullable = false, length = 36)
    val memberId: String,
    @Column(name = "terms_id", nullable = false, length = 36)
    val termsId: String,
) : BaseTimeEntity() {
    fun toDomain(): TermsAgreement =
        TermsAgreement(
            id = this.id,
            memberId = this.memberId,
            termsId = this.termsId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(agreement: TermsAgreement): TermsAgreementEntity =
            TermsAgreementEntity(
                id = agreement.id,
                memberId = agreement.memberId,
                termsId = agreement.termsId,
            )
    }
}
