package com.linktrip.application.domain.video

data class VideoAnalysisResult(
    val valid: Boolean,
    val destination: String?,
    val title: String?,
    val summary: String?,
    val estimatedMinCost: Long?,
    val estimatedMaxCost: Long?,
    val costBasis: CostBasis?,
    val hashtags: List<String>,
    val days: List<DaySchedule>,
    val timeline: List<TimelineItem>,
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

    data class TimelineItem(
        val timestampSeconds: Int,
        val description: String,
    )
}
