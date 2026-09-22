package com.linktrip.application.domain.terms

import com.linktrip.application.port.output.persistence.TermsPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class TermsServiceTest {
    @Mock
    lateinit var termsPersistencePort: TermsPersistencePort

    @InjectMocks
    lateinit var service: TermsService

    private val serviceTerms =
        Terms(
            id = "t-service",
            type = TermsType.SERVICE,
            title = "서비스 이용약관 동의",
            required = true,
            version = 1,
            detailUrl = "https://example.com/service",
        )
    private val privacyTerms =
        Terms(
            id = "t-privacy",
            type = TermsType.PRIVACY,
            title = "개인정보 수집·이용 동의",
            required = true,
            version = 1,
            detailUrl = "https://example.com/privacy",
        )

    @Test
    fun `약관을 조회하면_동의한 약관은 agreed true로_안 한 약관은 false로 내려간다`() {
        // given - SERVICE만 동의한 회원
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(serviceTerms, privacyTerms))
        whenever(termsPersistencePort.findAgreedTermsIds("m1", listOf("t-service", "t-privacy")))
            .thenReturn(setOf("t-service"))

        // when
        val result = service.getTerms("m1")

        // then
        assertEquals(2, result.size)
        assertTrue(result.first { it.terms.type == TermsType.SERVICE }.agreed)
        assertFalse(result.first { it.terms.type == TermsType.PRIVACY }.agreed)
    }

    @Test
    fun `필수 약관에 모두 동의하면_동의 이력이 저장된다`() {
        // given
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(serviceTerms, privacyTerms))
        whenever(termsPersistencePort.findAgreedTermsIds(any(), any())).thenReturn(emptySet())

        // when
        service.agree("m1", listOf(TermsType.SERVICE, TermsType.PRIVACY))

        // then - 두 약관 모두 이력 저장
        val captor = argumentCaptor<List<TermsAgreement>>()
        verify(termsPersistencePort).saveAgreements(captor.capture())
        val saved = captor.firstValue
        assertEquals(2, saved.size)
        assertEquals(setOf("t-service", "t-privacy"), saved.map { it.termsId }.toSet())
        assertTrue(saved.all { it.memberId == "m1" })
    }

    @Test
    fun `필수 약관이 빠지면_BAD_REQUEST_TERMS_REQUIRED 예외가 발생한다`() {
        // given - PRIVACY(필수) 누락
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(serviceTerms, privacyTerms))

        // when & then
        val exception =
            assertThrows<LinktripException> {
                service.agree("m1", listOf(TermsType.SERVICE))
            }
        assertEquals(ExceptionCode.BAD_REQUEST_TERMS_REQUIRED, exception.exceptionCode)
        verify(termsPersistencePort, never()).saveAgreements(any())
    }

    @Test
    fun `이미 동의한 약관이 포함되어도_새 이력만 저장하고 성공한다`() {
        // given - SERVICE는 이미 동의됨
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(serviceTerms, privacyTerms))
        whenever(termsPersistencePort.findAgreedTermsIds(any(), any())).thenReturn(setOf("t-service"))

        // when - 전체 동의 재요청
        service.agree("m1", listOf(TermsType.SERVICE, TermsType.PRIVACY))

        // then - PRIVACY만 새로 저장
        val captor = argumentCaptor<List<TermsAgreement>>()
        verify(termsPersistencePort).saveAgreements(captor.capture())
        assertEquals(listOf("t-privacy"), captor.firstValue.map { it.termsId })
    }

    @Test
    fun `모든 약관에 이미 동의한 상태로 재호출하면_저장 없이 성공한다`() {
        // given
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(serviceTerms, privacyTerms))
        whenever(termsPersistencePort.findAgreedTermsIds(any(), any()))
            .thenReturn(setOf("t-service", "t-privacy"))

        // when
        service.agree("m1", listOf(TermsType.SERVICE, TermsType.PRIVACY))

        // then - 멱등: 추가 저장 없음
        verify(termsPersistencePort, never()).saveAgreements(any())
    }

    @Test
    fun `active 약관에 없는 유형으로 동의하면_BAD_REQUEST_TERMS_TYPE 예외가 발생한다`() {
        // given - PRIVACY만 active인 상태
        whenever(termsPersistencePort.findAllActive()).thenReturn(listOf(privacyTerms))

        // when & then - SERVICE는 active에 없음
        val exception =
            assertThrows<LinktripException> {
                service.agree("m1", listOf(TermsType.SERVICE, TermsType.PRIVACY))
            }
        assertEquals(ExceptionCode.BAD_REQUEST_TERMS_TYPE, exception.exceptionCode)
    }
}
