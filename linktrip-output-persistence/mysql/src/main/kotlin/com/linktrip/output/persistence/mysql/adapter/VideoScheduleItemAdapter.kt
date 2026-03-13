package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.VideoScheduleItem
import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import com.linktrip.output.persistence.mysql.entity.PlaceEntity
import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity
import com.linktrip.output.persistence.mysql.repository.VideoScheduleItemJpaRepository
import org.springframework.stereotype.Component

@Component
class VideoScheduleItemAdapter(
    private val videoScheduleItemJpaRepository: VideoScheduleItemJpaRepository,
) : VideoScheduleItemPersistencePort {
    override fun saveAll(items: List<VideoScheduleItem>) {
        val entities = items.map { VideoScheduleItemEntity.from(it) }
        videoScheduleItemJpaRepository.saveAll(entities)
    }

    override fun findByVideoSummaryId(videoSummaryId: String): List<VideoScheduleItem> =
        videoScheduleItemJpaRepository
            .findByVideoSummaryIdOrderByDayAscItemOrderAsc(videoSummaryId)
            .map { it.toDomain() }

    override fun findByVideoSummaryIdWithPlace(videoSummaryId: String): List<VideoScheduleItem> =
        videoScheduleItemJpaRepository
            .findByVideoSummaryIdWithPlace(videoSummaryId)
            .map { row ->
                val item = (row[0] as VideoScheduleItemEntity).toDomain()
                val place = (row[1] as? PlaceEntity)?.toDomain()
                item.copy(place = place)
            }

    override fun findRetryableItems(videoSummaryId: String): List<VideoScheduleItem> =
        videoScheduleItemJpaRepository
            .findRetryableItems(videoSummaryId)
            .map { it.toDomain() }

    override fun findVideoSummaryIdsWithRetryableItems(): List<String> =
        videoScheduleItemJpaRepository.findVideoSummaryIdsWithRetryableItems()
}
