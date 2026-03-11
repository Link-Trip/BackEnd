package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.Place
import com.linktrip.application.domain.video.PlaceEnrichResult
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.repository.PlaceJpaRepository
import com.linktrip.output.persistence.mysql.repository.VideoScheduleItemJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PlaceEnrichPersistenceAdapter(
    private val videoScheduleItemJpaRepository: VideoScheduleItemJpaRepository,
    private val placeJpaRepository: PlaceJpaRepository,
) : PlaceEnrichPersistencePort {
    @Transactional
    override fun applyResults(
        videoSummaryId: String,
        results: List<PlaceEnrichResult>,
    ) {
        if (results.isEmpty()) return

        val itemMap = videoScheduleItemJpaRepository
            .findByVideoSummaryIdOrderByDayAscItemOrderAsc(videoSummaryId)
            .associateBy { it.id }

        results.forEach { result ->
            val itemEntity = itemMap[result.itemId] ?: return@forEach

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
