package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.trip.TripPlanItem
import com.linktrip.application.port.output.persistence.TripPlanItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanItemWithItinerary
import com.linktrip.output.persistence.mysql.entity.TripPlanItemEntity
import com.linktrip.output.persistence.mysql.repository.TripPlanItemJpaRepository
import com.linktrip.output.persistence.mysql.repository.TripPlanItemQuerydslRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TripPlanItemPersistenceAdapter(
    private val jpaRepository: TripPlanItemJpaRepository,
    private val querydslRepository: TripPlanItemQuerydslRepository,
) : TripPlanItemPersistencePort {
    override fun saveAll(items: List<TripPlanItem>) {
        jpaRepository.saveAll(items.map { TripPlanItemEntity.from(it) })
    }

    override fun findByTripPlanId(tripPlanId: String): List<TripPlanItem> =
        jpaRepository.findByTripPlanId(tripPlanId).map { it.toDomain() }

    override fun findActiveByTripPlanId(tripPlanId: String): List<TripPlanItem> =
        querydslRepository.findActiveByTripPlanId(tripPlanId).map { it.toDomain() }

    override fun findActiveWithItineraryAndPlaceByTripPlanId(tripPlanId: String): List<TripPlanItemWithItinerary> =
        querydslRepository.findActiveWithItineraryAndPlaceByTripPlanId(tripPlanId).map { row ->
            TripPlanItemWithItinerary(
                tripPlanItem = row.tripPlanItem.toDomain(),
                travelItineraryItem =
                    row.itineraryItem.toDomain().copy(
                        place = row.place?.toDomain(),
                    ),
            )
        }

    @Transactional
    override fun updateAll(items: List<TripPlanItem>) {
        val itemMap = items.associateBy { it.id }
        val entities = jpaRepository.findAllById(itemMap.keys)
        entities.forEach { entity ->
            val updated = itemMap[entity.id] ?: return@forEach
            entity.day = updated.day
            entity.itemOrder = updated.itemOrder
            entity.deleted = updated.deleted
        }
    }

    @Transactional
    override fun deleteByTripPlanId(tripPlanId: String) {
        querydslRepository.bulkDeleteByTripPlanId(tripPlanId)
    }

    override fun countActiveByTripPlanId(tripPlanId: String): Int =
        querydslRepository.countActiveByTripPlanId(tripPlanId).toInt()
}
