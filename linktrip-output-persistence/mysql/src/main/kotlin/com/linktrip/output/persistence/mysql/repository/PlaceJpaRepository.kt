package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceJpaRepository : JpaRepository<PlaceEntity, String> {
    fun findByGooglePlaceId(googlePlaceId: String): PlaceEntity?
}
