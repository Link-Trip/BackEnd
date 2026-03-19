package com.linktrip.application.domain.member

import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.application.port.output.persistence.MemberPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock
    lateinit var memberPort: MemberPort

    @Mock
    lateinit var tokenProvider: TokenProvider

    @InjectMocks
    lateinit var service: AuthService

    @Test
    fun `이미 가입된 시리얼 번호로 인증하면_기존 회원 정보를 조회하고_새로 저장하지 않으며_isNewMember가 false이다`() {
        // given - 이미 가입된 시리얼 번호의 회원
        val existingMember = Member(id = "member-1", serialNumber = "serial-123")
        whenever(memberPort.findBySerialNumber("serial-123")).thenReturn(existingMember)
        whenever(tokenProvider.create("member-1")).thenReturn("token-abc")

        // when - 기존 시리얼 번호로 인증한다
        val result = service.authenticateBySerial("serial-123")

        // then - 기존 회원 정보를 반환하고, 새로 저장하지 않으며, isNewMember가 false이다
        assertEquals("member-1", result.memberId)
        assertEquals("token-abc", result.accessToken)
        assertFalse(result.isNewMember)
        verify(memberPort, never()).save(any())
    }

    @Test
    fun `처음 보는 시리얼 번호로 인증하면_새 회원을 생성하여 저장하고_isNewMember가 true이다`() {
        // given - DB에 존재하지 않는 신규 시리얼 번호
        whenever(memberPort.findBySerialNumber("new-serial")).thenReturn(null)
        val savedMember = Member(id = "new-member-1", serialNumber = "new-serial")
        whenever(memberPort.save(any())).thenReturn(savedMember)
        whenever(tokenProvider.create("new-member-1")).thenReturn("new-token")

        // when - 신규 시리얼 번호로 인증한다
        val result = service.authenticateBySerial("new-serial")

        // then - 새 회원이 저장되고, isNewMember가 true이다
        assertEquals("new-member-1", result.memberId)
        assertEquals("new-token", result.accessToken)
        assertTrue(result.isNewMember)
        verify(memberPort).save(any())
    }

    @Test
    fun `기존 회원이든 신규 회원이든_인증 시 항상 memberId 기반의 JWT 토큰이 생성된다`() {
        // given - 기존 회원
        val member = Member(id = "m1", serialNumber = "s1")
        whenever(memberPort.findBySerialNumber("s1")).thenReturn(member)
        whenever(tokenProvider.create("m1")).thenReturn("token")

        // when - 인증을 수행한다
        service.authenticateBySerial("s1")

        // then - memberId 기반으로 토큰이 생성된다
        verify(tokenProvider).create("m1")
    }
}
