package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.VideoAnalysisResult

data class VideoAnalyzeResponse(
    val days: List<DayResponse>,
) {
    data class DayResponse(
        val day: Int,
        val items: List<ItemResponse>,
    )

    data class ItemResponse(
        val order: Int,
        val category: String,
        val name: String,
        val description: String?,
        val tips: String?,
    )

    companion object {
        fun from(result: VideoAnalysisResult): VideoAnalyzeResponse =
            VideoAnalyzeResponse(
                days =
                    result.days.map { day ->
                        DayResponse(
                            day = day.day,
                            items =
                                day.items.map { item ->
                                    ItemResponse(
                                        order = item.order,
                                        category = item.category.name,
                                        name = item.name,
                                        description = item.description,
                                        tips = item.tips,
                                    )
                                },
                        )
                    },
            )
    }
}
