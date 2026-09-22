package com.linktrip.application.port.input

import com.linktrip.application.domain.terms.Terms
import com.linktrip.application.domain.terms.TermsType

interface TermsUseCase {
    fun getTerms(memberId: String): List<TermsWithAgreement>

    fun agree(
        memberId: String,
        types: List<TermsType>,
    )

    data class TermsWithAgreement(
        val terms: Terms,
        val agreed: Boolean,
    )
}
