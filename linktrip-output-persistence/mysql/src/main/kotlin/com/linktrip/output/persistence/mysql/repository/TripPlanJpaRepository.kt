package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TripPlanEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TripPlanJpaRepository : JpaRepository<TripPlanEntity, String> {
    fun findByMemberIdOrderByCreatedAtDesc(memberId: String): List<TripPlanEntity>

    fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean
}
