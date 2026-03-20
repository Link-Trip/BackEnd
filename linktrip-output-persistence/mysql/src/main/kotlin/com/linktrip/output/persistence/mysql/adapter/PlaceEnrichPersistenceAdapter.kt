package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.Place
import com.linktrip.application.domain.video.PlaceEnrichResult
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.repository.PlaceJpaRepository
import com.linktrip.output.persistence.mysql.repository.TravelItineraryItemQuerydslRepository
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class PlaceEnrichPersistenceAdapter(
    private val itineraryItemQuerydslRepository: TravelItineraryItemQuerydslRepository,
    private val placeJpaRepository: PlaceJpaRepository,
) : PlaceEnrichPersistencePort {
    @Transactional
    override fun applyResults(
        videoAnalysisTaskId: String,
        results: List<PlaceEnrichResult>,
    ) {
        if (results.isEmpty()) return

        val itemMap =
            itineraryItemQuerydslRepository
                .findByVideoAnalysisTaskIdOrderByDayAscItemOrderAsc(videoAnalysisTaskId)
                .associateBy { it.id }

        results.forEach { result ->
            val itemEntity = itemMap[result.itemId]
            if (itemEntity == null) {
                logger.warn { "장소 보강 대상 아이템 없음: itemId=${result.itemId}" }
                return@forEach
            }

            val place = result.place
            if (result.success && place != null) {
                val placeEntity =
                    placeJpaRepository.findByGooglePlaceId(place.googlePlaceId)
                        ?: placeJpaRepository.save(PlaceEntity.from(Place.from(place)))

                itemEntity.placeId = placeEntity.id
            } else {
                itemEntity.placeSearchCount += 1
            }
        }
    }
}
