package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.HashtagPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.persistence.VideoTimelinePersistencePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoAnalysisResultSaver(
    private val travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val videoTimelinePersistencePort: VideoTimelinePersistencePort,
    private val hashtagPersistencePort: HashtagPersistencePort,
) {
    @Transactional
    fun save(
        videoAnalysisTaskId: String,
        itineraryItems: List<TravelItineraryItem>,
        summary: String? = null,
        estimatedMinCost: Long? = null,
        estimatedMaxCost: Long? = null,
        costBasis: CostBasis? = null,
        hashtags: List<String> = emptyList(),
        timelines: List<VideoTimeline> = emptyList(),
        destination: String? = null,
    ) {
        travelItineraryItemPersistencePort.saveAll(itineraryItems)
        videoTimelinePersistencePort.saveAll(timelines)
        videoAnalysisTaskPersistencePort.updateValidAndStatus(
            videoAnalysisTaskId,
            valid = true,
            VideoAnalysisTaskStatus.COMPLETED,
            summary = summary,
            estimatedMinCost = estimatedMinCost,
            estimatedMaxCost = estimatedMaxCost,
            costBasis = costBasis,
            destination = destination,
        )
        saveHashtags(videoAnalysisTaskId, hashtags)
    }

    private fun saveHashtags(
        videoAnalysisTaskId: String,
        hashtagNames: List<String>,
    ) {
        if (hashtagNames.isEmpty()) return

        val existingHashtags = hashtagPersistencePort.findByNames(hashtagNames)
        val existingNames = existingHashtags.map { it.name }.toSet()

        val newHashtags =
            hashtagNames
                .filter { it !in existingNames }
                .map { Hashtag.create(it) }
        val savedNewHashtags =
            if (newHashtags.isNotEmpty()) hashtagPersistencePort.saveAll(newHashtags) else emptyList()

        val taskHashtags =
            (existingHashtags + savedNewHashtags).map { hashtag ->
                VideoAnalysisTaskHashtag.create(videoAnalysisTaskId, hashtag.id)
            }
        hashtagPersistencePort.saveAllTaskHashtags(taskHashtags)
    }
}
