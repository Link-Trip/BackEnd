package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.Category
import com.linktrip.application.domain.video.VideoScheduleItem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "video_schedule_item",
    indexes = [
        Index(name = "idx_schedule_item_video_summary_id", columnList = "video_summary_id"),
    ],
)
class VideoScheduleItemEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "video_summary_id", nullable = false, length = 36)
    val videoSummaryId: String,
    @Column(name = "day", nullable = false)
    val day: Int,
    @Column(name = "item_order", nullable = false)
    val itemOrder: Int,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    val category: Category,
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    @Column(name = "description", length = 500)
    val description: String? = null,
    @Column(name = "tips", length = 500)
    val tips: String? = null,
    @Column(name = "place_id", length = 36)
    var placeId: String? = null,
    @Column(name = "place_search_count", nullable = false)
    var placeSearchCount: Int = 0,
) : BaseTimeEntity() {
    fun toDomain(): VideoScheduleItem =
        VideoScheduleItem(
            id = this.id,
            videoSummaryId = this.videoSummaryId,
            day = this.day,
            itemOrder = this.itemOrder,
            category = this.category,
            name = this.name,
            description = this.description,
            tips = this.tips,
            placeId = this.placeId,
            placeSearchCount = this.placeSearchCount,
        )

    companion object {
        fun from(item: VideoScheduleItem): VideoScheduleItemEntity =
            VideoScheduleItemEntity(
                id = item.id,
                videoSummaryId = item.videoSummaryId,
                day = item.day,
                itemOrder = item.itemOrder,
                category = item.category,
                name = item.name,
                description = item.description,
                tips = item.tips,
                placeId = item.placeId,
                placeSearchCount = item.placeSearchCount,
            )
    }
}
