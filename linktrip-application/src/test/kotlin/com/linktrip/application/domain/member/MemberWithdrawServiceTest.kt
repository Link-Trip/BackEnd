package com.linktrip.application.domain.member

import com.linktrip.application.domain.trip.TripPlan
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.application.port.output.persistence.TripPlanItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
class MemberWithdrawServiceTest {
    @Mock
    lateinit var memberPort: MemberPort

    @Mock
    lateinit var tripPlanPersistencePort: TripPlanPersistencePort

    @Mock
    lateinit var tripPlanItemPersistencePort: TripPlanItemPersistencePort

    @InjectMocks
    lateinit var service: MemberWithdrawService

    @Test
    fun `탈퇴하면_시리얼이 비가역 마스킹되고_기기 정보가 제거되고_여행 계획이 모두 삭제된다`() {
        // given - 여행 계획 2건과 FCM 토큰이 있는 회원
        val member =
            Member(
                id = "m1",
                serialNumber = "device-serial-123",
                fcmToken = "fcm-token",
                platform = Platform.IOS,
            )
        whenever(memberPort.findById("m1")).thenReturn(member)
        val plans =
            listOf(
                TripPlan(id = "p1", memberId = "m1", videoAnalysisTaskId = "t1", title = "도쿄"),
                TripPlan(id = "p2", memberId = "m1", videoAnalysisTaskId = "t2", title = "오사카"),
            )
        whenever(tripPlanPersistencePort.findByMemberId("m1")).thenReturn(plans)
        whenever(memberPort.withdraw(any())).thenAnswer { it.arguments[0] }

        // when - 탈퇴한다
        val result = service.withdraw("m1")

        // then - 삭제된 일정 수가 반환되고, 마스킹된 회원이 저장된다
        assertEquals(2, result.deletedTripPlanCount)
        verify(tripPlanItemPersistencePort).deleteByTripPlanId("p1")
        verify(tripPlanItemPersistencePort).deleteByTripPlanId("p2")
        verify(tripPlanPersistencePort).deleteById("p1")
        verify(tripPlanPersistencePort).deleteById("p2")

        val captor = argumentCaptor<Member>()
        verify(memberPort).withdraw(captor.capture())
        val withdrawn = captor.firstValue
        assertEquals("DELETED_m1", withdrawn.serialNumber)
        assertNull(withdrawn.fcmToken)
        assertNull(withdrawn.platform)
        assertTrue(withdrawn.isWithdrawn)
    }

    @Test
    fun `이미 탈퇴한 회원이 다시 호출하면_아무 작업 없이 0건을 반환한다`() {
        // given - 이미 마스킹된 회원
        val withdrawn = Member(id = "m1", serialNumber = "DELETED_m1")
        whenever(memberPort.findById("m1")).thenReturn(withdrawn)

        // when - 다시 탈퇴를 호출한다
        val result = service.withdraw("m1")

        // then - 멱등하게 0건 반환, 추가 작업 없음
        assertEquals(0, result.deletedTripPlanCount)
        verify(tripPlanPersistencePort, never()).findByMemberId(any())
        verify(memberPort, never()).withdraw(any())
    }

    @Test
    fun `여행 계획이 없는 회원이 탈퇴하면_0건 반환과 함께 마스킹만 수행된다`() {
        // given - 일정이 없는 회원
        val member = Member(id = "m2", serialNumber = "serial-2")
        whenever(memberPort.findById("m2")).thenReturn(member)
        whenever(tripPlanPersistencePort.findByMemberId("m2")).thenReturn(emptyList())
        whenever(memberPort.withdraw(any())).thenAnswer { it.arguments[0] }

        // when
        val result = service.withdraw("m2")

        // then
        assertEquals(0, result.deletedTripPlanCount)
        verify(memberPort).withdraw(any())
        verify(tripPlanItemPersistencePort, never()).deleteByTripPlanId(any())
    }

    @Test
    fun `존재하지 않는 회원이 탈퇴하면_NOT_FOUND_MEMBER 예외가 발생한다`() {
        // given
        whenever(memberPort.findById("unknown")).thenReturn(null)

        // when & then
        val exception = assertThrows<LinktripException> { service.withdraw("unknown") }
        assertEquals(ExceptionCode.NOT_FOUND_MEMBER, exception.exceptionCode)
    }
}
