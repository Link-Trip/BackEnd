package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.output.persistence.mysql.entity.TravelItineraryItemEntity
import com.linktrip.output.persistence.mysql.repository.TravelItineraryItemJpaRepository
import com.linktrip.output.persistence.mysql.repository.TravelItineraryItemQuerydslRepository
import org.springframework.stereotype.Component

@Component("travelItineraryItemDbAdapter")
class TravelItineraryItemAdapter(
    private val jpaRepository: TravelItineraryItemJpaRepository,
    private val querydslRepository: TravelItineraryItemQuerydslRepository,
) : TravelItineraryItemPersistencePort {
    override fun saveAll(items: List<TravelItineraryItem>) {
        val entities = items.map { TravelItineraryItemEntity.from(it) }
        jpaRepository.saveAll(entities)
    }

    override fun findByVideoAnalysisTaskId(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        querydslRepository
            .findByVideoAnalysisTaskIdOrderByDayAscItemOrderAsc(videoAnalysisTaskId)
            .map { it.toDomain() }

    override fun findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        querydslRepository
            .findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId)
            .map { row ->
                row.item.toDomain().copy(place = row.place?.toDomain())
            }

    override fun findRetryableItems(videoAnalysisTaskId: String): List<TravelItineraryItem> =
        querydslRepository
            .findRetryableItems(videoAnalysisTaskId)
            .map { it.toDomain() }

    override fun findVideoAnalysisTaskIdsWithRetryableItems(): List<String> =
        querydslRepository.findVideoAnalysisTaskIdsWithRetryableItems()
}
