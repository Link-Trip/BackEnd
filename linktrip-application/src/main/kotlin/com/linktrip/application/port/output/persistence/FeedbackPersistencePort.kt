package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.feedback.Feedback
import java.time.LocalDateTime

interface FeedbackPersistencePort {
    fun save(feedback: Feedback): Feedback

    fun countByMemberIdAndCreatedAtBetween(
        memberId: String,
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): Long
}
