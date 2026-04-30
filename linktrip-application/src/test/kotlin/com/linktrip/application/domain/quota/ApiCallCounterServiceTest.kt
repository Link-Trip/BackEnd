package com.linktrip.application.domain.quota

import com.linktrip.application.domain.notification.CostAlertEvent
import com.linktrip.application.port.output.notification.NotificationPort
import com.linktrip.application.port.output.quota.ApiCallCountPersistencePort
import com.linktrip.application.port.output.quota.ApiCostPolicyPort
import com.linktrip.application.port.output.quota.ApiDailyCostAlertPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ApiCallCounterServiceTest {
    @Mock
    lateinit var countPort: ApiCallCountPersistencePort

    @Mock
    lateinit var costPolicyPort: ApiCostPolicyPort

    @Mock
    lateinit var alertPort: ApiDailyCostAlertPersistencePort

    @Mock
    lateinit var notificationPort: NotificationPort

    @InjectMocks
    lateinit var service: ApiCallCounterService

    @Test
    fun `recordSuccess 는 ApiType 과 오늘 날짜로 카운트를 increment 한다`() {
        stubBreakdown(geminiCount = 0L)

        service.recordSuccess(ApiType.GEMINI)

        val captor = argumentCaptor<ApiCallCount>()
        verify(countPort).increment(captor.capture())
        assertEquals(ApiType.GEMINI, captor.firstValue.apiType)
        assertEquals(LocalDate.now(), captor.firstValue.callDate)
        assertEquals(1L, captor.firstValue.callCount)
    }

    @Test
    fun `누적 비용이 임계값 1000원 미달이면_Discord 알림과 임계값 갱신을 모두 수행하지 않는다`() {
        // 30 * 50 = 1500 → 1500 < (lastSent=0) + 1000 = false → 알림 발송 케이스
        // 일부러 미달 케이스: 10 * 50 = 500 < 1000 → 발송 X
        stubBreakdown(geminiCount = 10L)
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(null)

        service.recordSuccess(ApiType.GEMINI)

        verify(notificationPort, never()).sendCostAlert(any())
        verify(alertPort, never()).upsert(any())
    }

    @Test
    fun `누적 비용이 첫 1000원 임계값을 넘으면_thresholdKrw 1000 으로 알림을 발송하고 lastSent 를 갱신한다`() {
        // 30 * 50 = 1500 → newThreshold = (1500 / 1000) * 1000 = 1000
        stubBreakdown(geminiCount = 30L)
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(null)

        service.recordSuccess(ApiType.GEMINI)

        val eventCaptor = argumentCaptor<CostAlertEvent>()
        verify(notificationPort).sendCostAlert(eventCaptor.capture())
        assertEquals(1000L, eventCaptor.firstValue.thresholdKrw)
        assertEquals(1500L, eventCaptor.firstValue.breakdown.totalKrw)

        val alertCaptor = argumentCaptor<ApiDailyCostAlert>()
        verify(alertPort).upsert(alertCaptor.capture())
        assertEquals(1000L, alertCaptor.firstValue.lastSentThresholdKrw)
    }

    @Test
    fun `누적 비용이 0원에서 5500원으로 한 번에 점프하면_5000원 임계값으로 단 1회만 발송한다 (중간 임계값 압축)`() {
        // 110 * 50 = 5500 → newThreshold = 5000. 1000/2000/3000/4000 알림은 의도적으로 압축됨.
        stubBreakdown(geminiCount = 110L)
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(null)

        service.recordSuccess(ApiType.GEMINI)

        val captor = argumentCaptor<CostAlertEvent>()
        verify(notificationPort).sendCostAlert(captor.capture())
        assertEquals(5000L, captor.firstValue.thresholdKrw)
        // 알림은 단 1회
        verify(notificationPort, org.mockito.Mockito.times(1)).sendCostAlert(any())
    }

    @Test
    fun `같은 임계값 구간 내 재호출은_중복 알림을 발송하지 않는다 (lastSent=1000 + 누적=1200 → return)`() {
        // 1200 < (lastSent=1000) + 1000 = 2000 → 발송 X
        stubBreakdown(geminiCount = 24L) // 24 * 50 = 1200
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(1000L)

        service.recordSuccess(ApiType.GEMINI)

        verify(notificationPort, never()).sendCostAlert(any())
    }

    @Test
    fun `Discord 알림 발송이 실패해도_recordSuccess 는 외부로 예외를 전파하지 않는다 (알림은 best-effort)`() {
        // increment 는 이미 성공한 상태인데, 알림 실패로 호출자가 깨지면 외부 API 결과까지 손실되어 손해가 큼.
        // 메모상 알림은 log only 정책.
        stubBreakdown(geminiCount = 30L)
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(null)
        whenever(notificationPort.sendCostAlert(any())).thenThrow(RuntimeException("Discord 다운"))

        // throw 하지 않고 정상 종료
        service.recordSuccess(ApiType.GEMINI)

        // increment 는 정상 호출되어야 함 (카운트 자체는 적재됨)
        verify(countPort).increment(any())
    }

    @Test
    fun `카운트 increment 자체가 실패하면_예외를 그대로 전파한다 (fail-loud - 가드 신뢰성)`() {
        // counter 의 drift 는 가드 신뢰성을 깨뜨려 비용 폭주로 이어지므로 fail-loud.
        whenever(countPort.increment(any())).thenThrow(RuntimeException("DB 다운"))

        assertThrows<RuntimeException> {
            service.recordSuccess(ApiType.GEMINI)
        }
    }

    @Test
    fun `비용 breakdown 은 findAllByDate 로 1회만 조회되어야 한다 (N+1 회피)`() {
        // ApiType 별로 findByApiTypeAndDate 를 N 번 부르지 않고, 일자 기준 1쿼리로 모두 가져온다.
        stubBreakdown(geminiCount = 30L)
        whenever(alertPort.findLastSentThresholdKrw(LocalDate.now())).thenReturn(null)

        service.recordSuccess(ApiType.GEMINI)

        verify(countPort).findAllByDate(LocalDate.now())
        verify(countPort, never()).findByApiTypeAndDate(any(), any())
    }

    /**
     * 단가는 GEMINI=50, YOUTUBE_DATA=0, GOOGLE_PLACES=7 (yml 기본값과 동일) 로 고정.
     * 각 카운트는 인자로 받아 totalKrw 를 의도적으로 조절.
     */
    private fun stubBreakdown(
        geminiCount: Long = 0L,
        youtubeDataCount: Long = 0L,
        googlePlacesCount: Long = 0L,
    ) {
        whenever(countPort.findAllByDate(LocalDate.now())).thenReturn(
            listOf(
                ApiCallCount(id = "g", apiType = ApiType.GEMINI, callDate = LocalDate.now(), callCount = geminiCount),
                ApiCallCount(
                    id = "y",
                    apiType = ApiType.YOUTUBE_DATA,
                    callDate = LocalDate.now(),
                    callCount = youtubeDataCount,
                ),
                ApiCallCount(
                    id = "p",
                    apiType = ApiType.GOOGLE_PLACES,
                    callDate = LocalDate.now(),
                    callCount = googlePlacesCount,
                ),
            ),
        )
        whenever(costPolicyPort.perCallKrw(ApiType.GEMINI)).thenReturn(50L)
        whenever(costPolicyPort.perCallKrw(ApiType.YOUTUBE_DATA)).thenReturn(0L)
        whenever(costPolicyPort.perCallKrw(ApiType.GOOGLE_PLACES)).thenReturn(7L)
    }
}
