package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.GooglePlacesPort
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PlaceEnrichService(
    private val googlePlacesPort: GooglePlacesPort,
    private val placeEnrichPersistencePort: PlaceEnrichPersistencePort,
    private val travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val placeEnrichDispatcher: CoroutineDispatcher,
) {
    fun enrichPlaces(
        videoAnalysisTaskId: String,
        destination: String? = null,
    ) {
        val items = travelItineraryItemPersistencePort.findRetryableItems(videoAnalysisTaskId)

        if (items.isEmpty()) {
            logger.info { "장소 보강 대상 없음: videoAnalysisTaskId=$videoAnalysisTaskId" }
            return
        }

        logger.info {
            "장소 보강 시작: videoAnalysisTaskId=$videoAnalysisTaskId, destination=$destination, items=${items.size}"
        }

        val results =
            runBlocking(placeEnrichDispatcher) {
                items.map { item ->
                    async { searchPlace(item, destination) }
                }.awaitAll()
            }

        placeEnrichPersistencePort.applyResults(videoAnalysisTaskId, results)

        logger.info { "장소 보강 완료: videoAnalysisTaskId=$videoAnalysisTaskId" }
    }

    private fun searchPlace(
        item: TravelItineraryItem,
        destination: String?,
    ): PlaceEnrichResult =
        try {
            val place = googlePlacesPort.searchPlace(item.name, destination)
            PlaceEnrichResult(itemId = item.id, place = place, success = true)
        } catch (e: Exception) {
            logger.warn(e) { "장소 검색 API 실패: name=${item.name}" }
            PlaceEnrichResult(itemId = item.id, place = null, success = false)
        }

    fun retryAll() {
        val videoAnalysisTaskIds = travelItineraryItemPersistencePort.findVideoAnalysisTaskIdsWithRetryableItems()

        if (videoAnalysisTaskIds.isEmpty()) {
            logger.info { "장소 보강 리트라이 대상 없음" }
            return
        }

        logger.info { "장소 보강 리트라이 시작: ${videoAnalysisTaskIds.size}건" }

        videoAnalysisTaskIds.forEach { videoAnalysisTaskId ->
            try {
                val destination = videoAnalysisTaskPersistencePort.findById(videoAnalysisTaskId)?.destination
                enrichPlaces(videoAnalysisTaskId, destination)
            } catch (e: Exception) {
                logger.error(e) { "장소 보강 리트라이 실패: videoAnalysisTaskId=$videoAnalysisTaskId" }
            }
        }

        logger.info { "장소 보강 리트라이 완료" }
    }
}
