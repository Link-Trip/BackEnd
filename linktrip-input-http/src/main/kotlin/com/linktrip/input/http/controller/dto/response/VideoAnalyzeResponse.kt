package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.Place
import com.linktrip.application.domain.video.PlaceStatus
import com.linktrip.application.domain.video.VideoScheduleItem
import com.linktrip.application.domain.video.VideoSummary

data class VideoAnalyzeResponse(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: String,
    val placeEnrichmentCompleted: Boolean,
    val scheduleItems: List<ScheduleItemResponse>,
) {
    data class ScheduleItemResponse(
        val id: String,
        val day: Int,
        val order: Int,
        val category: String,
        val name: String,
        val description: String?,
        val tips: String?,
        val place: PlaceResponse?,
        val placeStatus: String,
    )

    data class PlaceResponse(
        val id: String,
        val name: String,
        val googlePlaceId: String,
        val address: String?,
        val latitude: Double?,
        val longitude: Double?,
    ) {
        companion object {
            fun from(place: Place): PlaceResponse =
                PlaceResponse(
                    id = place.id,
                    name = place.name,
                    googlePlaceId = place.googlePlaceId,
                    address = place.address,
                    latitude = place.latitude,
                    longitude = place.longitude,
                )
        }
    }

    companion object {
        fun from(
            videoSummary: VideoSummary,
            items: List<VideoScheduleItem>,
        ): VideoAnalyzeResponse =
            VideoAnalyzeResponse(
                id = videoSummary.id,
                youtubeUrl = videoSummary.youtubeUrl,
                valid = videoSummary.valid,
                status = videoSummary.status.name,
                placeEnrichmentCompleted = items.isNotEmpty() && items.all { it.isResolved() },
                scheduleItems =
                    items.map { item ->
                        ScheduleItemResponse(
                            id = item.id,
                            day = item.day,
                            order = item.itemOrder,
                            category = item.category.name,
                            name = item.name,
                            description = item.description,
                            tips = item.tips,
                            place = item.place?.let { PlaceResponse.from(it) },
                            placeStatus = PlaceStatus.from(item).name,
                        )
                    },
            )
    }
}
