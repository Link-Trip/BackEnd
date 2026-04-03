package com.linktrip.application.domain.member

import com.linktrip.application.port.output.auth.OAuthInfo
import com.linktrip.application.port.output.auth.OAuthPort
import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.application.port.output.persistence.MemberPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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

    @Mock
    lateinit var kakaoOAuthPort: OAuthPort

    lateinit var service: AuthService

    @BeforeEach
    fun setUp() {
        whenever(kakaoOAuthPort.getProviderType()).thenReturn(ProviderType.KAKAO)
        val memberService = MemberService(memberPort)
        service = AuthService(listOf(kakaoOAuthPort), memberService, tokenProvider)
    }

    @Test
    fun `이미 가입된 프로바이더로 로그인하면 기존 회원 정보를 반환하고 isNewMember가 false이다`() {
        // given
        val oAuthInfo = OAuthInfo(ProviderType.KAKAO, "kakao-123", "test@email.com")
        whenever(kakaoOAuthPort.requestUserInfo("access-token")).thenReturn(oAuthInfo)

        val existingMember =
            Member(
                id = "member-1",
                email = "test@email.com",
                providerType = ProviderType.KAKAO,
                providerId = "kakao-123",
            )
        whenever(memberPort.findByProviderTypeAndProviderId(ProviderType.KAKAO, "kakao-123"))
            .thenReturn(existingMember)
        whenever(tokenProvider.create("member-1")).thenReturn("token-abc")

        // when
        val result = service.socialLogin(ProviderType.KAKAO, "access-token")

        // then
        assertEquals("member-1", result.memberId)
        assertEquals("token-abc", result.accessToken)
        assertFalse(result.isNewMember)
        verify(memberPort, never()).save(any())
    }

    @Test
    fun `신규 프로바이더로 로그인하면 새 회원을 생성하고 isNewMember가 true이다`() {
        // given
        val oAuthInfo = OAuthInfo(ProviderType.KAKAO, "kakao-456", "new@email.com")
        whenever(kakaoOAuthPort.requestUserInfo("new-access-token")).thenReturn(oAuthInfo)
        whenever(memberPort.findByProviderTypeAndProviderId(ProviderType.KAKAO, "kakao-456"))
            .thenReturn(null)

        val savedMember =
            Member(
                id = "new-member-1",
                email = "new@email.com",
                providerType = ProviderType.KAKAO,
                providerId = "kakao-456",
            )
        whenever(memberPort.save(any())).thenReturn(savedMember)
        whenever(tokenProvider.create("new-member-1")).thenReturn("new-token")

        // when
        val result = service.socialLogin(ProviderType.KAKAO, "new-access-token")

        // then
        assertEquals("new-member-1", result.memberId)
        assertEquals("new-token", result.accessToken)
        assertTrue(result.isNewMember)
        verify(memberPort).save(any())
    }

    @Test
    fun `소셜 로그인 시 항상 memberId 기반의 JWT 토큰이 생성된다`() {
        // given
        val oAuthInfo = OAuthInfo(ProviderType.KAKAO, "kakao-789", "user@email.com")
        whenever(kakaoOAuthPort.requestUserInfo("token")).thenReturn(oAuthInfo)

        val member =
            Member(
                id = "m1",
                email = "user@email.com",
                providerType = ProviderType.KAKAO,
                providerId = "kakao-789",
            )
        whenever(memberPort.findByProviderTypeAndProviderId(ProviderType.KAKAO, "kakao-789"))
            .thenReturn(member)
        whenever(tokenProvider.create("m1")).thenReturn("jwt-token")

        // when
        service.socialLogin(ProviderType.KAKAO, "token")

        // then
        verify(tokenProvider).create("m1")
    }
}
