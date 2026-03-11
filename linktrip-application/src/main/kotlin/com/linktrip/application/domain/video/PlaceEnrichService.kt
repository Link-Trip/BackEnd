package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.GooglePlacesPort
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PlaceEnrichService(
    private val googlePlacesPort: GooglePlacesPort,
    private val placeEnrichPersistencePort: PlaceEnrichPersistencePort,
    private val videoScheduleItemPersistencePort: VideoScheduleItemPersistencePort,
) {
    fun enrichPlaces(
        videoSummaryId: String,
        destination: String? = null,
    ) {
        val items = videoScheduleItemPersistencePort.findRetryableItems(videoSummaryId)

        if (items.isEmpty()) {
            logger.info { "장소 보강 대상 없음: videoSummaryId=$videoSummaryId" }
            return
        }

        logger.info { "장소 보강 시작: videoSummaryId=$videoSummaryId, destination=$destination, items=${items.size}" }

        // Phase 1: 트랜잭션 밖 - Google Places API 호출 (destination으로 검색 정확도 향상)
        val results =
            items.map { item ->
                try {
                    val place = googlePlacesPort.searchPlace(item.name, destination)
                    PlaceEnrichResult(itemId = item.id, place = place, success = true)
                } catch (e: Exception) {
                    logger.warn(e) { "장소 검색 API 실패: name=${item.name}" }
                    PlaceEnrichResult(itemId = item.id, place = null, success = false)
                }
            }

        // Phase 2: 트랜잭션 안 - DB 저장 (Place + ScheduleItem)
        placeEnrichPersistencePort.applyResults(videoSummaryId, results)

        logger.info { "장소 보강 완료: videoSummaryId=$videoSummaryId" }
    }
}
