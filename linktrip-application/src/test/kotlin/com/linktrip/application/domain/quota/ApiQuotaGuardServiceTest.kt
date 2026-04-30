package com.linktrip.application.domain.quota

import com.linktrip.application.port.output.quota.ApiCallCountPersistencePort
import com.linktrip.application.port.output.quota.ApiQuotaPolicyPort
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
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ApiQuotaGuardServiceTest {
    @Mock
    lateinit var countPort: ApiCallCountPersistencePort

    @Mock
    lateinit var policyPort: ApiQuotaPolicyPort

    @InjectMocks
    lateinit var service: ApiQuotaGuardService

    @Test
    fun `정책 한도가 미설정인 ApiType 으로 isExceeded 호출 시_INTERNAL_QUOTA_POLICY_NOT_CONFIGURED 가 발생한다 (fail-closed)`() {
        // 운영 yml 누락이나 환경변수 오타 등으로 한도가 비면, 가드가 조용히 무력화되어 비용 폭주 사고로 이어진다.
        // counter 의 fail-loud 정책과 일관되게 본 가드도 throw 로 멈춰야 한다.
        whenever(policyPort.dailyLimit(ApiType.GEMINI)).thenReturn(null)

        val exception =
            assertThrows<LinktripException> {
                service.isExceeded(ApiType.GEMINI)
            }
        assertEquals(ExceptionCode.INTERNAL_QUOTA_POLICY_NOT_CONFIGURED, exception.exceptionCode)
    }

    @Test
    fun `현재 카운트가 한도 미만이면_isExceeded 가 false 를 반환한다`() {
        whenever(policyPort.dailyLimit(ApiType.GEMINI)).thenReturn(500L)
        whenever(countPort.findByApiTypeAndDate(ApiType.GEMINI, LocalDate.now()))
            .thenReturn(apiCallCount(ApiType.GEMINI, callCount = 100L))

        assertFalse(service.isExceeded(ApiType.GEMINI))
    }

    @Test
    fun `현재 카운트가 한도와 정확히 같으면_isExceeded 가 true 를 반환한다 (경계값 - inclusive 차단)`() {
        // 한도 도달 = 더 이상 호출 금지. 한도 정확히 도달한 시점부터 차단되어야 함.
        whenever(policyPort.dailyLimit(ApiType.GEMINI)).thenReturn(500L)
        whenever(countPort.findByApiTypeAndDate(ApiType.GEMINI, LocalDate.now()))
            .thenReturn(apiCallCount(ApiType.GEMINI, callCount = 500L))

        assertTrue(service.isExceeded(ApiType.GEMINI))
    }

    @Test
    fun `현재 카운트가 한도를 초과하면_isExceeded 가 true 를 반환한다`() {
        whenever(policyPort.dailyLimit(ApiType.GEMINI)).thenReturn(500L)
        whenever(countPort.findByApiTypeAndDate(ApiType.GEMINI, LocalDate.now()))
            .thenReturn(apiCallCount(ApiType.GEMINI, callCount = 999L))

        assertTrue(service.isExceeded(ApiType.GEMINI))
    }

    @Test
    fun `해당 일자의 카운트 row 가 아직 없으면_0 으로 간주하여 isExceeded 가 false 를 반환한다`() {
        // 자정 직후 그날 첫 호출 시점 — row 가 없어도 정상 통과되어야 (가드가 잘못 막으면 안 됨).
        whenever(policyPort.dailyLimit(ApiType.GEMINI)).thenReturn(500L)
        whenever(countPort.findByApiTypeAndDate(ApiType.GEMINI, LocalDate.now())).thenReturn(null)

        assertFalse(service.isExceeded(ApiType.GEMINI))
    }

    @Test
    fun `모든 ApiType 의 카운트가 한도 미만이면_isAnyApiExceeded 가 false 를 반환한다`() {
        ApiType.entries.forEach { apiType ->
            whenever(policyPort.dailyLimit(apiType)).thenReturn(1000L)
            whenever(countPort.findByApiTypeAndDate(apiType, LocalDate.now()))
                .thenReturn(apiCallCount(apiType, callCount = 100L))
        }

        assertFalse(service.isAnyApiExceeded())
    }

    @Test
    fun `하나의 ApiType 만 한도를 초과해도_isAnyApiExceeded 가 true 를 반환한다 (큐 컨슈머는 어느 하나라도 막히면 dequeue 멈춤)`() {
        // GEMINI / YOUTUBE_DATA 정상, GOOGLE_PLACES 만 초과. 마지막 ApiType 까지 순회되어야 검출되는 것을 보장.
        ApiType.entries.forEach { apiType ->
            whenever(policyPort.dailyLimit(apiType)).thenReturn(500L)
            val count = if (apiType == ApiType.GOOGLE_PLACES) 1000L else 100L
            whenever(countPort.findByApiTypeAndDate(apiType, LocalDate.now()))
                .thenReturn(apiCallCount(apiType, callCount = count))
        }

        assertTrue(service.isAnyApiExceeded())
    }

    private fun apiCallCount(
        apiType: ApiType,
        callCount: Long,
    ): ApiCallCount =
        ApiCallCount(
            id = "id-${apiType.name}",
            apiType = apiType,
            callDate = LocalDate.now(),
            callCount = callCount,
        )
}
