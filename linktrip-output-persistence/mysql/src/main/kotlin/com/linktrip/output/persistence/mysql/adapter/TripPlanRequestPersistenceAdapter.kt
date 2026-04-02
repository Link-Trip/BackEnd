package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.trip.TripPlanRequest
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.output.persistence.mysql.entity.TripPlanRequestEntity
import com.linktrip.output.persistence.mysql.repository.TripPlanRequestJpaRepository
import com.linktrip.output.persistence.mysql.repository.TripPlanRequestQuerydslRepository
import org.springframework.stereotype.Component

@Component
class TripPlanRequestPersistenceAdapter(
    private val jpaRepository: TripPlanRequestJpaRepository,
    private val querydslRepository: TripPlanRequestQuerydslRepository,
) : TripPlanRequestPersistencePort {
    override fun save(request: TripPlanRequest): TripPlanRequest =
        jpaRepository.save(TripPlanRequestEntity.from(request)).toDomain()

    override fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean = jpaRepository.existsByMemberIdAndVideoAnalysisTaskId(memberId, videoAnalysisTaskId)

    override fun findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TripPlanRequest> =
        querydslRepository.findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId).map { it.toDomain() }

    override fun findMemberIdsByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<String> =
        querydslRepository.findMemberIdsByVideoAnalysisTaskId(videoAnalysisTaskId)

    override fun saveAll(requests: List<TripPlanRequest>) {
        jpaRepository.saveAll(requests.map { TripPlanRequestEntity.from(it) })
    }
}
