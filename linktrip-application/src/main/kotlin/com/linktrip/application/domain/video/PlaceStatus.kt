package com.linktrip.application.domain.video

enum class PlaceStatus {
    FOUND,
    PENDING,
    SEARCHING,
    NOT_FOUND,
    NOT_REQUIRED,
    ;

    companion object {
        fun from(item: TravelItineraryItem): PlaceStatus =
            when {
                item.category == Category.TRANSPORTATION -> NOT_REQUIRED
                item.placeId != null -> FOUND
                item.placeSearchCount == 0 -> PENDING
                item.placeSearchCount >= TravelItineraryItem.MAX_PLACE_SEARCH_COUNT -> NOT_FOUND
                else -> SEARCHING
            }
    }
}
