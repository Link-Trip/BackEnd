package com.linktrip.output.http.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.linktrip.application.domain.video.Category
import com.linktrip.application.domain.video.VideoAnalysisResult

@JsonIgnoreProperties(ignoreUnknown = true)
data class AiApiResponse(
    val valid: Boolean,
    val destination: String?,
    val days: List<DayDto>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DayDto(
        val day: Int?,
        val items: List<ItemDto>?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ItemDto(
        val order: Int?,
        val category: String?,
        val name: String?,
        val description: String?,
        val tips: String?,
    )

    fun toDomain(): VideoAnalysisResult =
        VideoAnalysisResult(
            valid = this.valid,
            destination = this.destination,
            days =
                this.days?.mapIndexed { dayIndex, dayDto ->
                    VideoAnalysisResult.DaySchedule(
                        day = dayDto.day ?: (dayIndex + 1),
                        items =
                            dayDto.items?.mapIndexed { itemIndex, itemDto ->
                                VideoAnalysisResult.ScheduleItem(
                                    order = itemDto.order ?: (itemIndex + 1),
                                    category = parseCategory(itemDto.category),
                                    name = itemDto.name ?: "",
                                    description = itemDto.description,
                                    tips = itemDto.tips,
                                )
                            } ?: emptyList(),
                    )
                } ?: emptyList(),
        )

    private fun parseCategory(category: String?): Category =
        try {
            Category.valueOf(category?.uppercase() ?: "EAT")
        } catch (_: IllegalArgumentException) {
            Category.EAT
        }
}
