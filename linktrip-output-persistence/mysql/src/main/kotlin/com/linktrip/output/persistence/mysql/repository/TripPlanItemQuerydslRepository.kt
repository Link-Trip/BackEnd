package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QPlaceEntity
import com.linktrip.output.persistence.mysql.entity.QTravelItineraryItemEntity
import com.linktrip.output.persistence.mysql.entity.QTripPlanItemEntity
import com.linktrip.output.persistence.mysql.entity.TripPlanItemEntity
import com.linktrip.output.persistence.mysql.repository.dto.TripPlanItemWithItineraryAndPlace
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripPlanItemQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val tripPlanItem = QTripPlanItemEntity.tripPlanItemEntity
    private val itineraryItem = QTravelItineraryItemEntity.travelItineraryItemEntity
    private val place = QPlaceEntity.placeEntity

    fun findActiveByTripPlanId(tripPlanId: String): List<TripPlanItemEntity> =
        queryFactory
            .selectFrom(tripPlanItem)
            .where(
                tripPlanItem.tripPlanId.eq(tripPlanId),
                tripPlanItem.deleted.isFalse,
            )
            .orderBy(
                tripPlanItem.day.asc(),
                tripPlanItem.itemOrder.asc(),
            )
            .fetch()

    fun findActiveWithItineraryAndPlaceByTripPlanId(tripPlanId: String): List<TripPlanItemWithItineraryAndPlace> =
        queryFactory
            .select(tripPlanItem, itineraryItem, place)
            .from(tripPlanItem)
            .join(itineraryItem).on(
                tripPlanItem.travelItineraryItemId.eq(itineraryItem.id),
                itineraryItem.deleted.isFalse,
            )
            .leftJoin(place).on(
                itineraryItem.placeId.eq(place.id),
                place.deleted.isFalse,
            )
            .where(
                tripPlanItem.tripPlanId.eq(tripPlanId),
                tripPlanItem.deleted.isFalse,
            )
            .orderBy(
                tripPlanItem.day.asc(),
                tripPlanItem.itemOrder.asc(),
            )
            .fetch()
            .map { tuple ->
                TripPlanItemWithItineraryAndPlace(
                    tripPlanItem = tuple.get(tripPlanItem)!!,
                    itineraryItem = tuple.get(itineraryItem)!!,
                    place = tuple.get(place),
                )
            }

    fun countActiveByTripPlanId(tripPlanId: String): Long =
        queryFactory
            .select(tripPlanItem.count())
            .from(tripPlanItem)
            .where(
                tripPlanItem.tripPlanId.eq(tripPlanId),
                tripPlanItem.deleted.isFalse,
            )
            .fetchOne() ?: 0L
}
