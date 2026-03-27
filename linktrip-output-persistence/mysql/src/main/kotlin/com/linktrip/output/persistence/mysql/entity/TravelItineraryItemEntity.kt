package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.Category
import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PreRemove
import jakarta.persistence.Table

@Entity
@Table(
    name = "travel_itinerary_item",
    indexes = [
        Index(
            name = "idx_itinerary_item_task_deleted_day_order",
            columnList = "video_analysis_task_id, deleted, day, item_order",
        ),
    ],
)
class TravelItineraryItemEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "video_analysis_task_id", nullable = false, length = 36)
    val videoAnalysisTaskId: String,
    @Column(name = "day", nullable = false)
    val day: Int,
    @Column(name = "item_order", nullable = false)
    val itemOrder: Int,
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
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
    @PreRemove
    fun preventDeletion() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    override fun softDelete() {
        throw LinktripException(ExceptionCode.INTERNAL_IMMUTABLE_DATA_DELETE)
    }

    fun toDomain(): TravelItineraryItem =
        TravelItineraryItem(
            id = this.id,
            videoAnalysisTaskId = this.videoAnalysisTaskId,
            day = this.day,
            itemOrder = this.itemOrder,
            category = this.category,
            name = this.name,
            description = this.description,
            tips = this.tips,
            placeId = this.placeId,
            placeSearchCount = this.placeSearchCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(item: TravelItineraryItem): TravelItineraryItemEntity =
            TravelItineraryItemEntity(
                id = item.id,
                videoAnalysisTaskId = item.videoAnalysisTaskId,
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
