package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TravelItineraryItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TravelItineraryItemJpaRepository : JpaRepository<TravelItineraryItemEntity, String>
