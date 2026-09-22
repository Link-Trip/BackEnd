package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.terms.Terms
import com.linktrip.application.domain.terms.TermsAgreement

interface TermsPersistencePort {
    /** 현재 노출 대상(active) 약관 목록 */
    fun findAllActive(): List<Terms>

    /** 회원이 동의한 약관 id 중 [termsIds] 에 포함된 것 */
    fun findAgreedTermsIds(
        memberId: String,
        termsIds: List<String>,
    ): Set<String>

    fun saveAgreements(agreements: List<TermsAgreement>)
}
