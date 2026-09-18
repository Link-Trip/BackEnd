package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.feedback.Feedback
import com.linktrip.application.port.output.persistence.FeedbackPersistencePort
import com.linktrip.output.persistence.mysql.entity.FeedbackEntity
import com.linktrip.output.persistence.mysql.repository.FeedbackJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class FeedbackPersistenceAdapter(
    private val feedbackJpaRepository: FeedbackJpaRepository,
) : FeedbackPersistencePort {
    override fun save(feedback: Feedback): Feedback =
        feedbackJpaRepository.save(FeedbackEntity.from(feedback)).toDomain()

    override fun countByMemberIdAndCreatedAtBetween(
        memberId: String,
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): Long =
        feedbackJpaRepository.countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            memberId,
            startInclusive,
            endExclusive,
        )
}
