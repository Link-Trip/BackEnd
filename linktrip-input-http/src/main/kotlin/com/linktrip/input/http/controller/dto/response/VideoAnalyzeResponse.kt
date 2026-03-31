package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.Place
import com.linktrip.application.domain.video.PlaceStatus
import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.application.domain.video.VideoTimeline

data class VideoAnalyzeResponse(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: String,
    val summary: String?,
    val estimatedMinCost: Long?,
    val estimatedMaxCost: Long?,
    val costBasis: String?,
    val placeEnrichmentCompleted: Boolean,
    val timelines: List<TimelineResponse>,
    val itineraryItems: List<ScheduleItemResponse>,
) {
    data class TimelineResponse(
        val timestampSeconds: Int,
        val timestamp: String,
        val timestampUrl: String,
        val description: String,
    ) {
        companion object {
            fun from(
                timeline: VideoTimeline,
                youtubeUrl: String,
            ): TimelineResponse =
                TimelineResponse(
                    timestampSeconds = timeline.timestampSeconds,
                    timestamp = formatTimestamp(timeline.timestampSeconds),
                    timestampUrl = "$youtubeUrl&t=${timeline.timestampSeconds}",
                    description = timeline.description,
                )

            private fun formatTimestamp(seconds: Int): String {
                val h = seconds / 3600
                val m = (seconds % 3600) / 60
                val s = seconds % 60
                return if (h > 0) {
                    "%d:%02d:%02d".format(h, m, s)
                } else {
                    "%d:%02d".format(m, s)
                }
            }
        }
    }

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
            videoAnalysisTask: VideoAnalysisTask,
            items: List<TravelItineraryItem>,
            timelines: List<VideoTimeline>,
        ): VideoAnalyzeResponse =
            VideoAnalyzeResponse(
                id = videoAnalysisTask.id,
                youtubeUrl = videoAnalysisTask.youtubeUrl,
                valid = videoAnalysisTask.valid,
                status = videoAnalysisTask.status.name,
                summary = videoAnalysisTask.summary,
                estimatedMinCost = videoAnalysisTask.estimatedMinCost,
                estimatedMaxCost = videoAnalysisTask.estimatedMaxCost,
                costBasis = videoAnalysisTask.costBasis?.name,
                placeEnrichmentCompleted =
                    videoAnalysisTask.status == VideoAnalysisTaskStatus.COMPLETED &&
                        items.isNotEmpty() &&
                        items.all { it.isResolved() },
                timelines = timelines.map { TimelineResponse.from(it, videoAnalysisTask.youtubeUrl) },
                itineraryItems =
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
