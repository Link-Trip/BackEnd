package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator

data class VideoScheduleItem(
    val id: String,
    val videoSummaryId: String,
    val day: Int,
    val itemOrder: Int,
    val category: Category,
    val name: String,
    val description: String?,
    val tips: String?,
    val placeId: String? = null,
    val placeSearchCount: Int = 0,
    val place: Place? = null,
) {
    fun isRetryable(): Boolean =
        placeId == null &&
            category != Category.TRANSPORTATION &&
            placeSearchCount < MAX_PLACE_SEARCH_COUNT

    fun isResolved(): Boolean = !isRetryable()

    companion object {
        const val MAX_PLACE_SEARCH_COUNT = 10

        fun from(
            videoSummaryId: String,
            daySchedule: VideoAnalysisResult.DaySchedule,
            item: VideoAnalysisResult.ScheduleItem,
        ): VideoScheduleItem =
            VideoScheduleItem(
                id = IdGenerator.generate(),
                videoSummaryId = videoSummaryId,
                day = daySchedule.day,
                itemOrder = item.order,
                category = item.category,
                name = item.name,
                description = item.description,
                tips = item.tips,
            )
    }
}
