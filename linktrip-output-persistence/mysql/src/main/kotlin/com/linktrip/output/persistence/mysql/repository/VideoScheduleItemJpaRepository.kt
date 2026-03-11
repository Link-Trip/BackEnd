package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.video.Category
import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VideoScheduleItemJpaRepository : JpaRepository<VideoScheduleItemEntity, String> {
    fun findByVideoSummaryIdOrderByDayAscItemOrderAsc(videoSummaryId: String): List<VideoScheduleItemEntity>

    @Query(
        """
        SELECT i, p FROM VideoScheduleItemEntity i
        LEFT JOIN PlaceEntity p ON i.placeId = p.id
        WHERE i.videoSummaryId = :videoSummaryId
        ORDER BY i.day ASC, i.itemOrder ASC
        """,
    )
    fun findByVideoSummaryIdWithPlace(videoSummaryId: String): List<Array<Any>>

    @Query(
        """
        SELECT e FROM VideoScheduleItemEntity e
        WHERE e.videoSummaryId = :videoSummaryId
          AND e.placeId IS NULL
          AND e.category <> :excludeCategory
          AND e.placeSearchCount < :maxSearchCount
        ORDER BY e.day ASC, e.itemOrder ASC
        """,
    )
    fun findRetryableItems(
        videoSummaryId: String,
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<VideoScheduleItemEntity>

    @Query(
        """
        SELECT DISTINCT e.videoSummaryId FROM VideoScheduleItemEntity e
        WHERE e.placeId IS NULL
          AND e.category <> :excludeCategory
          AND e.placeSearchCount < :maxSearchCount
        """,
    )
    fun findVideoSummaryIdsWithRetryableItems(
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<String>
}
