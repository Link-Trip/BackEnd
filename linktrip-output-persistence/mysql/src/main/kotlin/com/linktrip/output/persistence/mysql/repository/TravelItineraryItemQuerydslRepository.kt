package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.video.Category
import com.linktrip.output.persistence.mysql.entity.QPlaceEntity
import com.linktrip.output.persistence.mysql.entity.QTravelItineraryItemEntity
import com.linktrip.output.persistence.mysql.entity.TravelItineraryItemEntity
import com.linktrip.output.persistence.mysql.repository.dto.ScheduleItemWithPlace
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TravelItineraryItemQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val itineraryItem = QTravelItineraryItemEntity.travelItineraryItemEntity
    private val place = QPlaceEntity.placeEntity

    fun findByVideoAnalysisTaskIdOrderByDayAscItemOrderAsc(
        videoAnalysisTaskId: String,
    ): List<TravelItineraryItemEntity> =
        queryFactory
            .selectFrom(itineraryItem)
            .where(itineraryItem.videoAnalysisTaskId.eq(videoAnalysisTaskId))
            .orderBy(
                itineraryItem.day.asc(),
                itineraryItem.itemOrder.asc(),
            )
            .fetch()

    fun findByVideoAnalysisTaskIdWithPlace(videoAnalysisTaskId: String): List<ScheduleItemWithPlace> =
        queryFactory
            .select(itineraryItem, place)
            .from(itineraryItem)
            .leftJoin(place).on(itineraryItem.placeId.eq(place.id))
            .where(itineraryItem.videoAnalysisTaskId.eq(videoAnalysisTaskId))
            .orderBy(
                itineraryItem.day.asc(),
                itineraryItem.itemOrder.asc(),
            )
            .fetch()
            .map { tuple ->
                ScheduleItemWithPlace(
                    item = tuple.get(itineraryItem)!!,
                    place = tuple.get(place),
                )
            }

    fun findRetryableItems(
        videoAnalysisTaskId: String,
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<TravelItineraryItemEntity> =
        queryFactory
            .selectFrom(itineraryItem)
            .where(
                itineraryItem.videoAnalysisTaskId.eq(videoAnalysisTaskId),
                itineraryItem.placeId.isNull,
                itineraryItem.category.ne(excludeCategory),
                itineraryItem.placeSearchCount.lt(maxSearchCount),
            )
            .orderBy(
                itineraryItem.day.asc(),
                itineraryItem.itemOrder.asc(),
            )
            .fetch()

    fun findVideoAnalysisTaskIdsWithRetryableItems(
        excludeCategory: Category = Category.TRANSPORTATION,
        maxSearchCount: Int = 10,
    ): List<String> =
        queryFactory
            .selectDistinct(itineraryItem.videoAnalysisTaskId)
            .from(itineraryItem)
            .where(
                itineraryItem.placeId.isNull,
                itineraryItem.category.ne(excludeCategory),
                itineraryItem.placeSearchCount.lt(maxSearchCount),
            )
            .fetch()
}
