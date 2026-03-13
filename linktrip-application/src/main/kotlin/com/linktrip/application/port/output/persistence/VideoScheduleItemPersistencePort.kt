package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.VideoScheduleItem

interface VideoScheduleItemPersistencePort {
    fun saveAll(items: List<VideoScheduleItem>)

    fun findByVideoSummaryId(videoSummaryId: String): List<VideoScheduleItem>

    fun findByVideoSummaryIdWithPlace(videoSummaryId: String): List<VideoScheduleItem>

    fun findRetryableItems(videoSummaryId: String): List<VideoScheduleItem>

    fun findVideoSummaryIdsWithRetryableItems(): List<String>
}
