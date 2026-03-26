package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TripPlanItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TripPlanItemJpaRepository : JpaRepository<TripPlanItemEntity, String> {
    fun findByTripPlanId(tripPlanId: String): List<TripPlanItemEntity>

    fun deleteByTripPlanId(tripPlanId: String)
}
