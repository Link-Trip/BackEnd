package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.VideoScheduleItem
import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity
import com.linktrip.output.persistence.mysql.repository.VideoScheduleItemJpaRepository
import com.linktrip.output.persistence.mysql.repository.VideoScheduleItemQuerydslRepository
import org.springframework.stereotype.Component

@Component
class VideoScheduleItemAdapter(
    private val jpaRepository: VideoScheduleItemJpaRepository,
    private val querydslRepository: VideoScheduleItemQuerydslRepository,
) : VideoScheduleItemPersistencePort {
    override fun saveAll(items: List<VideoScheduleItem>) {
        val entities = items.map { VideoScheduleItemEntity.from(it) }
        jpaRepository.saveAll(entities)
    }

    override fun findByVideoSummaryId(videoSummaryId: String): List<VideoScheduleItem> =
        querydslRepository
            .findByVideoSummaryIdOrderByDayAscItemOrderAsc(videoSummaryId)
            .map { it.toDomain() }

    override fun findByVideoSummaryIdWithPlace(videoSummaryId: String): List<VideoScheduleItem> =
        querydslRepository
            .findByVideoSummaryIdWithPlace(videoSummaryId)
            .map { row ->
                row.item.toDomain().copy(place = row.place?.toDomain())
            }

    override fun findRetryableItems(videoSummaryId: String): List<VideoScheduleItem> =
        querydslRepository
            .findRetryableItems(videoSummaryId)
            .map { it.toDomain() }

    override fun findVideoSummaryIdsWithRetryableItems(): List<String> =
        querydslRepository.findVideoSummaryIdsWithRetryableItems()
}
