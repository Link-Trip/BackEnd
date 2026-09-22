package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.TermsEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TermsJpaRepository : JpaRepository<TermsEntity, String> {
    fun findAllByActiveTrueAndDeletedFalseOrderByCreatedAtAsc(): List<TermsEntity>
}
