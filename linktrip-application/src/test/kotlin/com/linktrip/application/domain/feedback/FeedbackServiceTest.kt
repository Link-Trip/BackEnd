package com.linktrip.application.domain.feedback

import com.linktrip.application.domain.member.Platform
import com.linktrip.application.domain.notification.FeedbackAlertEvent
import com.linktrip.application.port.input.FeedbackUseCase.CreateFeedbackCommand
import com.linktrip.application.port.output.persistence.FeedbackPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
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
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {
    @Mock
    lateinit var feedbackPersistencePort: FeedbackPersistencePort

    @Mock
    lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    lateinit var service: FeedbackService

    private fun command(content: String = "지도가 좋아요") =
        CreateFeedbackCommand(
            memberId = "m1",
            type = FeedbackType.SUGGESTION,
            content = content,
            appVersion = "1.0",
            platform = Platform.IOS,
            osVersion = "15",
            deviceModel = "Pixel 8",
        )

    @Test
    fun `의견을 전송하면_내용이 trim되어 저장되고_알림 이벤트가 발행된다`() {
        // given - 오늘 전송 이력이 없는 회원
        whenever(feedbackPersistencePort.countByMemberIdAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(0L)
        whenever(feedbackPersistencePort.save(any())).thenAnswer { it.arguments[0] }

        // when - 앞뒤 공백이 있는 내용으로 전송한다
        service.create(command(content = "  버그 있어요  "))

        // then - trim된 내용으로 저장되고 알림 이벤트가 발행된다
        val feedbackCaptor = argumentCaptor<Feedback>()
        verify(feedbackPersistencePort).save(feedbackCaptor.capture())
        assertEquals("버그 있어요", feedbackCaptor.firstValue.content)
        assertEquals(FeedbackType.SUGGESTION, feedbackCaptor.firstValue.type)

        val eventCaptor = argumentCaptor<FeedbackAlertEvent>()
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertEquals("버그 있어요", eventCaptor.firstValue.content)
        assertEquals("IOS", eventCaptor.firstValue.platform)
    }

    @Test
    fun `오늘 4회 전송한 회원은_5번째 전송이 가능하다`() {
        // given - 오늘 4회 전송한 회원
        whenever(feedbackPersistencePort.countByMemberIdAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(4L)
        whenever(feedbackPersistencePort.save(any())).thenAnswer { it.arguments[0] }

        // when & then - 정상 저장된다
        service.create(command())
        verify(feedbackPersistencePort).save(any())
    }

    @Test
    fun `오늘 5회 전송한 회원은_FEEDBACK_DAILY_LIMIT_EXCEEDED 예외가 발생한다`() {
        // given - 오늘 이미 5회 전송한 회원
        whenever(feedbackPersistencePort.countByMemberIdAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(5L)

        // when & then - 429 예외가 발생하고 저장·알림이 일어나지 않는다
        val exception = assertThrows<LinktripException> { service.create(command()) }
        assertEquals(ExceptionCode.FEEDBACK_DAILY_LIMIT_EXCEEDED, exception.exceptionCode)
        verify(feedbackPersistencePort, never()).save(any())
        verify(eventPublisher, never()).publishEvent(any<FeedbackAlertEvent>())
    }
}
