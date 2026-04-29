package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.quota.ApiType
import com.linktrip.output.persistence.mysql.entity.QApiCallCountEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ApiCallCountQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val apiCallCount = QApiCallCountEntity.apiCallCountEntity

    /**
     * (apiType, callDate) row 의 callCount 를 atomic 하게 1 증가.
     * row 가 없으면 0 을 반환하므로 어댑터에서 INSERT 분기로 넘어간다.
     */
    fun incrementCallCount(
        apiType: ApiType,
        callDate: LocalDate,
    ): Long =
        queryFactory
            .update(apiCallCount)
            .set(apiCallCount.callCount, apiCallCount.callCount.add(1))
            .where(
                apiCallCount.apiType.eq(apiType),
                apiCallCount.callDate.eq(callDate),
            )
            .execute()
}
