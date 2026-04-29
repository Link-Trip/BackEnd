package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.ApiDailyCostAlertEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApiDailyCostAlertJpaRepository : JpaRepository<ApiDailyCostAlertEntity, String> {
    fun findByAlertDate(alertDate: LocalDate): ApiDailyCostAlertEntity?
}
