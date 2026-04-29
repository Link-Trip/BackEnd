package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.quota.ApiCallCount
import com.linktrip.application.domain.quota.ApiType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "api_call_count",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_api_call_count_type_date", columnNames = ["api_type", "call_date"]),
    ],
    indexes = [
        Index(name = "idx_api_call_count_date_type", columnList = "call_date, api_type"),
    ],
)
class ApiCallCountEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false, length = 40)
    val apiType: ApiType,
    @Column(name = "call_date", nullable = false)
    val callDate: LocalDate,
    @Column(name = "call_count", nullable = false)
    var callCount: Long = 0L,
) : BaseTimeEntity() {
    fun toDomain(): ApiCallCount =
        ApiCallCount(
            id = this.id,
            apiType = this.apiType,
            callDate = this.callDate,
            callCount = this.callCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(apiCallCount: ApiCallCount): ApiCallCountEntity =
            ApiCallCountEntity(
                id = apiCallCount.id,
                apiType = apiCallCount.apiType,
                callDate = apiCallCount.callDate,
                callCount = apiCallCount.callCount,
            )
    }
}
