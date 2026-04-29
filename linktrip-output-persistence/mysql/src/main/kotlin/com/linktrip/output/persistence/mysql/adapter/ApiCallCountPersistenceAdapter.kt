package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.quota.ApiCallCount
import com.linktrip.application.domain.quota.ApiType
import com.linktrip.application.port.output.quota.ApiCallCountPersistencePort
import com.linktrip.output.persistence.mysql.entity.ApiCallCountEntity
import com.linktrip.output.persistence.mysql.repository.ApiCallCountJpaRepository
import com.linktrip.output.persistence.mysql.repository.ApiCallCountQuerydslRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 일별 API 호출 카운트 영속화 어댑터.
 *
 * update-first 패턴: 먼저 atomic UPDATE +1 시도, 영향 row 가 0 이면 (그날 첫 호출) 신규 INSERT.
 * - +1 은 [ApiCallCountQuerydslRepository.incrementCallCount] (QueryDSL UPDATE) 로 atomic 처리해 lost update 차단.
 * - row 가 없을 때 두 트랜잭션이 동시 진입하면 한쪽이 unique 제약 위반으로 rollback 되어 fail-loud.
 */
@Component
class ApiCallCountPersistenceAdapter(
    private val jpaRepository: ApiCallCountJpaRepository,
    private val querydslRepository: ApiCallCountQuerydslRepository,
) : ApiCallCountPersistencePort {
    @Transactional
    override fun increment(apiCallCount: ApiCallCount) {
        val updated = querydslRepository.incrementCallCount(apiCallCount.apiType, apiCallCount.callDate)
        if (updated == 0L) {
            jpaRepository.save(ApiCallCountEntity.from(apiCallCount))
        }
    }

    @Transactional(readOnly = true)
    override fun findByApiTypeAndDate(
        apiType: ApiType,
        date: LocalDate,
    ): ApiCallCount? = jpaRepository.findByApiTypeAndCallDate(apiType, date)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAllByDate(date: LocalDate): List<ApiCallCount> =
        jpaRepository.findAllByCallDate(date).map { it.toDomain() }
}
