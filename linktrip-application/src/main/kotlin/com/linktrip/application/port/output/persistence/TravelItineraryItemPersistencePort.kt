package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.TravelItineraryItem

interface TravelItineraryItemPersistencePort {
    fun saveAll(items: List<TravelItineraryItem>)

    fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TravelItineraryItem>

    fun findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId: String): List<TravelItineraryItem>

    fun findRetryableItems(videoAnalysisTaskId: String): List<TravelItineraryItem>

    fun findVideoAnalysisTaskIdsWithRetryableItems(): List<String>
}
