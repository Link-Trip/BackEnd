package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.video.Category
import com.linktrip.output.persistence.mysql.entity.QPlaceEntity
import com.linktrip.output.persistence.mysql.entity.QVideoScheduleItemEntity
import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity
import com.linktrip.output.persistence.mysql.repository.dto.ScheduleItemWithPlace
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class VideoScheduleItemQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val scheduleItem = QVideoScheduleItemEntity.videoScheduleItemEntity
    private val place = QPlaceEntity.placeEntity

    fun findByVideoSummaryIdOrderByDayAscItemOrderAsc(videoSummaryId: String): List<VideoScheduleItemEntity> =
        queryFactory
            .selectFrom(scheduleItem)
            .where(scheduleItem.videoSummaryId.eq(videoSummaryId))
            .orderBy(
                scheduleItem.day.asc(),
                scheduleItem.itemOrder.asc(),
            )
            .fetch()

    fun findByVideoSummaryIdWithPlace(videoSummaryId: String): List<ScheduleItemWithPlace> =
        queryFactory
            .select(scheduleItem, place)
            .from(scheduleItem)
            .leftJoin(place).on(scheduleItem.placeId.eq(place.id))
            .where(scheduleItem.videoSummaryId.eq(videoSummaryId))
            .orderBy(
                scheduleItem.day.asc(),
                scheduleItem.itemOrder.asc(),
            )
            .fetch()
            .map { tuple ->
                ScheduleItemWithPlace(
                    item = tuple.get(scheduleItem)!!,
                    place = tuple.get(place),
                )
            }

    fun findRetryableItems(
        videoSummaryId: String,
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<VideoScheduleItemEntity> =
        queryFactory
            .selectFrom(scheduleItem)
            .where(
                scheduleItem.videoSummaryId.eq(videoSummaryId),
                scheduleItem.placeId.isNull,
                scheduleItem.category.ne(excludeCategory),
                scheduleItem.placeSearchCount.lt(maxSearchCount),
            )
            .orderBy(
                scheduleItem.day.asc(),
                scheduleItem.itemOrder.asc(),
            )
            .fetch()

    fun findVideoSummaryIdsWithRetryableItems(
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<String> =
        queryFactory
            .selectDistinct(scheduleItem.videoSummaryId)
            .from(scheduleItem)
            .where(
                scheduleItem.placeId.isNull,
                scheduleItem.category.ne(excludeCategory),
                scheduleItem.placeSearchCount.lt(maxSearchCount),
            )
            .fetch()
}
