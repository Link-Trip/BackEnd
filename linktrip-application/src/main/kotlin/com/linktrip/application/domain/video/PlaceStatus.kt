package com.linktrip.application.domain.video

enum class PlaceStatus {
    FOUND,
    PENDING,
    SEARCHING,
    NOT_FOUND,
    NOT_REQUIRED,
    ;

    companion object {
        private const val MAX_PLACE_SEARCH_COUNT = 10

        fun from(item: VideoScheduleItem): PlaceStatus =
            when {
                item.category == Category.TRANSPORTATION -> NOT_REQUIRED
                item.placeId != null -> FOUND
                item.placeSearchCount == 0 -> PENDING
                item.placeSearchCount >= MAX_PLACE_SEARCH_COUNT -> NOT_FOUND
                else -> SEARCHING
            }
    }
}
