package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.trip.TripPlanRequest

interface TripPlanRequestPersistencePort {
    fun save(request: TripPlanRequest): TripPlanRequest

    fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean

    fun findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TripPlanRequest>

    fun findMemberIdsByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<String>

    fun saveAll(requests: List<TripPlanRequest>)
}
