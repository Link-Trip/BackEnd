package com.linktrip.application.domain.video

data class VideoAnalysisResult(
    val valid: Boolean,
    val days: List<DaySchedule>,
) {
    data class DaySchedule(
        val day: Int,
        val items: List<ScheduleItem>,
    )

    data class ScheduleItem(
        val order: Int,
        val category: Category,
        val name: String,
        val description: String?,
        val tips: String?,
    )

    enum class Category {
        EAT,
        ATTRACTION,
        SHOPPING,
        TRANSPORTATION,
    }
}
