package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TripPlanRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TripPlanRequestJpaRepository : JpaRepository<TripPlanRequestEntity, String> {
    fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean
}
