package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.quota.ApiType
import com.linktrip.output.persistence.mysql.entity.ApiCallCountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApiCallCountJpaRepository : JpaRepository<ApiCallCountEntity, String> {
    fun findByApiTypeAndCallDate(
        apiType: ApiType,
        callDate: LocalDate,
    ): ApiCallCountEntity?

    fun findAllByCallDate(callDate: LocalDate): List<ApiCallCountEntity>
}
