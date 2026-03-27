package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.HashtagEntity
import org.springframework.data.jpa.repository.JpaRepository

interface HashtagJpaRepository : JpaRepository<HashtagEntity, String> {
    fun findByNameIn(names: List<String>): List<HashtagEntity>
}
