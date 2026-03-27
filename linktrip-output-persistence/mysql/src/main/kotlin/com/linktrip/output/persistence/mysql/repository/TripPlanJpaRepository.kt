package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TripPlanEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TripPlanJpaRepository : JpaRepository<TripPlanEntity, String> {
    fun findByIdAndDeletedFalse(id: String): TripPlanEntity?

    fun findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId: String): List<TripPlanEntity>

    fun existsByMemberIdAndVideoAnalysisTaskIdAndDeletedFalse(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean
}
