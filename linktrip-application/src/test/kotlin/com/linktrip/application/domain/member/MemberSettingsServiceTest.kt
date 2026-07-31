package com.linktrip.application.domain.member

import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
class MemberSettingsServiceTest {
    @Mock
    lateinit var memberPort: MemberPort

    @InjectMocks
    lateinit var service: MemberSettingsService

    @Test
    fun `FCM 토큰을 등록하면_회원에 토큰과 플랫폼이 저장된다`() {
        // given - 가입된 회원
        val member = Member(id = "m1", serialNumber = "s1")
        whenever(memberPort.findById("m1")).thenReturn(member)
        whenever(memberPort.update(any())).thenAnswer { it.arguments[0] }

        // when - FCM 토큰을 등록한다
        service.registerFcmToken("m1", "fcm-token-123", Platform.IOS)

        // then - 토큰과 플랫폼이 반영된 회원이 저장된다
        val captor = argumentCaptor<Member>()
        verify(memberPort).update(captor.capture())
        assertEquals("fcm-token-123", captor.firstValue.fcmToken)
        assertEquals(Platform.IOS, captor.firstValue.platform)
    }

    @Test
    fun `기존 토큰이 있어도_새 토큰으로 갱신된다`() {
        // given - 이미 토큰이 등록된 회원
        val member =
            Member(
                id = "m1",
                serialNumber = "s1",
                fcmToken = "old-token",
                platform = Platform.ANDROID,
            )
        whenever(memberPort.findById("m1")).thenReturn(member)
        whenever(memberPort.update(any())).thenAnswer { it.arguments[0] }

        // when - 새 토큰을 등록한다
        service.registerFcmToken("m1", "new-token", Platform.IOS)

        // then - 새 토큰과 플랫폼으로 갱신된다
        val captor = argumentCaptor<Member>()
        verify(memberPort).update(captor.capture())
        assertEquals("new-token", captor.firstValue.fcmToken)
        assertEquals(Platform.IOS, captor.firstValue.platform)
    }

    @Test
    fun `존재하지 않는 회원의 FCM 토큰을 등록하면_NOT_FOUND_MEMBER 예외가 발생한다`() {
        // given - 존재하지 않는 회원
        whenever(memberPort.findById("unknown")).thenReturn(null)

        // when & then - 예외가 발생하고 저장되지 않는다
        val exception =
            assertThrows<LinktripException> {
                service.registerFcmToken("unknown", "token", Platform.IOS)
            }
        assertEquals(ExceptionCode.NOT_FOUND_MEMBER, exception.exceptionCode)
        verify(memberPort, never()).update(any())
    }

    @Test
    fun `알림을 끄면_notificationEnabled가 false로 저장되고 변경된 값이 반환된다`() {
        // given - 알림이 켜져 있는 회원
        val member = Member(id = "m1", serialNumber = "s1", notificationEnabled = true)
        whenever(memberPort.findById("m1")).thenReturn(member)
        whenever(memberPort.update(any())).thenAnswer { it.arguments[0] }

        // when - 알림을 끈다
        val result = service.updateNotificationEnabled("m1", false)

        // then - false가 저장되고 반환된다
        assertFalse(result)
        val captor = argumentCaptor<Member>()
        verify(memberPort).update(captor.capture())
        assertFalse(captor.firstValue.notificationEnabled)
    }

    @Test
    fun `존재하지 않는 회원의 알림 설정을 변경하면_NOT_FOUND_MEMBER 예외가 발생한다`() {
        // given - 존재하지 않는 회원
        whenever(memberPort.findById("unknown")).thenReturn(null)

        // when & then - 예외가 발생한다
        val exception =
            assertThrows<LinktripException> {
                service.updateNotificationEnabled("unknown", true)
            }
        assertEquals(ExceptionCode.NOT_FOUND_MEMBER, exception.exceptionCode)
    }
}
