package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.trip.TripPlanRequest
import java.time.LocalDate

interface TripPlanRequestPersistencePort {
    fun save(request: TripPlanRequest): TripPlanRequest

    fun existsByMemberIdAndVideoAnalysisTaskId(
        memberId: String,
        videoAnalysisTaskId: String,
    ): Boolean

    fun findUnprocessedByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TripPlanRequest>

    fun findMemberIdsByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<String>

    fun saveAll(requests: List<TripPlanRequest>)

    fun countByMemberIdAndDate(
        memberId: String,
        date: LocalDate,
    ): Long
}
