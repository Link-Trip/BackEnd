package com.linktrip.application.domain.feedback

import com.linktrip.application.domain.notification.FeedbackAlertEvent
import com.linktrip.application.port.input.FeedbackUseCase
import com.linktrip.application.port.input.FeedbackUseCase.CreateFeedbackCommand
import com.linktrip.application.port.output.persistence.FeedbackPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
class FeedbackService(
    private val feedbackPersistencePort: FeedbackPersistencePort,
    private val eventPublisher: ApplicationEventPublisher,
) : FeedbackUseCase {
    @Transactional
    override fun create(command: CreateFeedbackCommand) {
        validateDailyLimit(command.memberId)

        val feedback = feedbackPersistencePort.save(Feedback.create(command))

        eventPublisher.publishEvent(
            FeedbackAlertEvent(
                type = feedback.type.name,
                content = feedback.content,
                appVersion = feedback.appVersion,
                platform = feedback.platform.name,
                osVersion = feedback.osVersion,
                deviceModel = feedback.deviceModel,
            ),
        )
    }

    private fun validateDailyLimit(memberId: String) {
        val today = LocalDate.now(KST)
        val count =
            feedbackPersistencePort.countByMemberIdAndCreatedAtBetween(
                memberId = memberId,
                startInclusive = today.atStartOfDay(),
                endExclusive = today.plusDays(1).atStartOfDay(),
            )
        if (count >= DAILY_LIMIT) {
            throw LinktripException(ExceptionCode.FEEDBACK_DAILY_LIMIT_EXCEEDED)
        }
    }

    companion object {
        private const val DAILY_LIMIT = 5
        private val KST = ZoneId.of("Asia/Seoul")
    }
}
