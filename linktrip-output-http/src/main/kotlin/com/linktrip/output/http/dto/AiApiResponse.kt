package com.linktrip.output.http.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.linktrip.application.domain.video.Category
import com.linktrip.application.domain.video.CostBasis
import com.linktrip.application.domain.video.VideoAnalysisResult

@JsonIgnoreProperties(ignoreUnknown = true)
data class AiApiResponse(
    val valid: Boolean,
    val destination: String?,
    val title: String?,
    val estimatedMinCost: Long?,
    val estimatedMaxCost: Long?,
    val costBasis: String?,
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
            title = this.title,
            estimatedMinCost = this.estimatedMinCost,
            estimatedMaxCost = this.estimatedMaxCost,
            costBasis = parseCostBasis(this.costBasis),
            days =
                this.days?.mapIndexed { dayIndex, dayDto ->
                    VideoAnalysisResult.DaySchedule(
                        day = dayDto.day ?: (dayIndex + 1),
                        items =
                            dayDto.items?.mapIndexedNotNull { itemIndex, itemDto ->
                                val name = itemDto.name?.trim()
                                if (name.isNullOrEmpty()) return@mapIndexedNotNull null

                                VideoAnalysisResult.ScheduleItem(
                                    order = itemDto.order ?: (itemIndex + 1),
                                    category = parseCategory(itemDto.category),
                                    name = name,
                                    description = itemDto.description,
                                    tips = itemDto.tips,
                                )
                            } ?: emptyList(),
                    )
                } ?: emptyList(),
        )

    private fun parseCostBasis(costBasis: String?): CostBasis? =
        try {
            costBasis?.trim()?.uppercase()?.let { CostBasis.valueOf(it) }
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun parseCategory(category: String?): Category {
        val normalized = category?.trim()?.uppercase() ?: return Category.EAT
        if (normalized == "TRANSPORTATION") return Category.TRANSPORTATION_TRANSIT
        return try {
            Category.valueOf(normalized)
        } catch (_: IllegalArgumentException) {
            Category.EAT
        }
    }
}
