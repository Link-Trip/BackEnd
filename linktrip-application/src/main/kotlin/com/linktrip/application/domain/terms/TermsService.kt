package com.linktrip.application.domain.terms

import com.linktrip.application.port.input.TermsUseCase
import com.linktrip.application.port.input.TermsUseCase.TermsWithAgreement
import com.linktrip.application.port.output.persistence.TermsPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 약관 조회·동의.
 *
 * 약관은 (type, version) 단위로 관리하며, 조회·동의는 항상 현재 active 버전을 대상으로 한다.
 * 약관이 개정되면 새 버전 행을 active 로 올리는 것만으로 기존 동의자는 agreed=false 가 되어
 * 클라이언트가 재동의 시트를 노출할 수 있다.
 */
@Service
class TermsService(
    private val termsPersistencePort: TermsPersistencePort,
) : TermsUseCase {
    @Transactional(readOnly = true)
    override fun getTerms(memberId: String): List<TermsWithAgreement> {
        val activeTerms = termsPersistencePort.findAllActive()
        val agreedTermsIds = termsPersistencePort.findAgreedTermsIds(memberId, activeTerms.map { it.id })
        return activeTerms.map { terms ->
            TermsWithAgreement(terms = terms, agreed = terms.id in agreedTermsIds)
        }
    }

    @Transactional
    override fun agree(
        memberId: String,
        types: List<TermsType>,
    ) {
        val activeTerms = termsPersistencePort.findAllActive()
        val activeByType = activeTerms.associateBy { it.type }

        val requestedTypes = types.toSet()
        if (requestedTypes.any { it !in activeByType }) {
            throw LinktripException(ExceptionCode.BAD_REQUEST_TERMS_TYPE)
        }

        val missingRequired =
            activeTerms.filter { it.required && it.type !in requestedTypes }
        if (missingRequired.isNotEmpty()) {
            throw LinktripException(ExceptionCode.BAD_REQUEST_TERMS_REQUIRED)
        }

        val targetTerms = requestedTypes.map { activeByType.getValue(it) }
        val alreadyAgreedIds = termsPersistencePort.findAgreedTermsIds(memberId, targetTerms.map { it.id })
        val newAgreements =
            targetTerms
                .filter { it.id !in alreadyAgreedIds }
                .map { TermsAgreement.create(memberId = memberId, termsId = it.id) }

        if (newAgreements.isNotEmpty()) {
            termsPersistencePort.saveAgreements(newAgreements)
            logger.info { "약관 동의 저장: memberId=$memberId, types=${requestedTypes.map { it.name }}" }
        }
    }
}
