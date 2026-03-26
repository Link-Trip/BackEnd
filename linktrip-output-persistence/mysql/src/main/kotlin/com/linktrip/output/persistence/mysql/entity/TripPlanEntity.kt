package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.trip.TripPlan
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "trip_plan",
    indexes = [
        Index(name = "idx_trip_plan_member_id", columnList = "member_id"),
        Index(name = "idx_trip_plan_video_analysis_task_id", columnList = "video_analysis_task_id"),
    ],
)
class TripPlanEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "member_id", nullable = false, length = 36)
    val memberId: String,
    @Column(name = "video_analysis_task_id", nullable = false, length = 36)
    val videoAnalysisTaskId: String,
    @Column(name = "title", nullable = false, length = 255)
    var title: String,
) : BaseTimeEntity() {
    fun toDomain(): TripPlan =
        TripPlan(
            id = this.id,
            memberId = this.memberId,
            videoAnalysisTaskId = this.videoAnalysisTaskId,
            title = this.title,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(tripPlan: TripPlan): TripPlanEntity =
            TripPlanEntity(
                id = tripPlan.id,
                memberId = tripPlan.memberId,
                videoAnalysisTaskId = tripPlan.videoAnalysisTaskId,
                title = tripPlan.title,
            )
    }
}
