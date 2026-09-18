package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.FeedbackEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface FeedbackJpaRepository : JpaRepository<FeedbackEntity, String> {
    fun countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        memberId: String,
        startInclusive: LocalDateTime,
        endExclusive: LocalDateTime,
    ): Long
}
