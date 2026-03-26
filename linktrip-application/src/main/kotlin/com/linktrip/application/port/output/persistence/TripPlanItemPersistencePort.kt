package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.trip.TripPlanItem
import com.linktrip.application.domain.video.TravelItineraryItem

interface TripPlanItemPersistencePort {
    fun saveAll(items: List<TripPlanItem>)

    fun findByTripPlanId(tripPlanId: String): List<TripPlanItem>

    fun findActiveByTripPlanId(tripPlanId: String): List<TripPlanItem>

    fun findActiveWithItineraryAndPlaceByTripPlanId(tripPlanId: String): List<TripPlanItemWithItinerary>

    fun updateAll(items: List<TripPlanItem>)

    fun deleteByTripPlanId(tripPlanId: String)

    fun countActiveByTripPlanId(tripPlanId: String): Int
}

data class TripPlanItemWithItinerary(
    val tripPlanItem: TripPlanItem,
    val travelItineraryItem: TravelItineraryItem,
)
