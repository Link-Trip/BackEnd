package com.linktrip.application.domain.terms

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class TermsAgreement(
    val id: String,
    val memberId: String,
    val termsId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            memberId: String,
            termsId: String,
        ): TermsAgreement =
            TermsAgreement(
                id = IdGenerator.generate(),
                memberId = memberId,
                termsId = termsId,
            )
    }
}
