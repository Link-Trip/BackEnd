package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.GooglePlacesPort
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

private val logger = KotlinLogging.logger {}

@Service
class PlaceEnrichService(
    private val googlePlacesPort: GooglePlacesPort,
    private val placeEnrichPersistencePort: PlaceEnrichPersistencePort,
    private val videoScheduleItemPersistencePort: VideoScheduleItemPersistencePort,
    private val placeEnrichExecutor: Executor,
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

        // Phase 1: 병렬 Google Places API 호출
        val futures =
            items.map { item ->
                CompletableFuture.supplyAsync({
                    try {
                        val place = googlePlacesPort.searchPlace(item.name, destination)
                        PlaceEnrichResult(itemId = item.id, place = place, success = true)
                    } catch (e: Exception) {
                        logger.warn(e) { "장소 검색 API 실패: name=${item.name}" }
                        PlaceEnrichResult(itemId = item.id, place = null, success = false)
                    }
                }, placeEnrichExecutor)
            }

        val results = futures.map { it.join() }

        // Phase 2: 트랜잭션 안 - DB 저장 (Place + ScheduleItem)
        placeEnrichPersistencePort.applyResults(videoSummaryId, results)

        logger.info { "장소 보강 완료: videoSummaryId=$videoSummaryId" }
    }

    fun retryAll() {
        val videoSummaryIds = videoScheduleItemPersistencePort.findVideoSummaryIdsWithRetryableItems()

        if (videoSummaryIds.isEmpty()) {
            logger.info { "장소 보강 리트라이 대상 없음" }
            return
        }

        logger.info { "장소 보강 리트라이 시작: ${videoSummaryIds.size}건" }

        videoSummaryIds.forEach { videoSummaryId ->
            try {
                enrichPlaces(videoSummaryId)
            } catch (e: Exception) {
                logger.error(e) { "장소 보강 리트라이 실패: videoSummaryId=$videoSummaryId" }
            }
        }

        logger.info { "장소 보강 리트라이 완료" }
    }
}
