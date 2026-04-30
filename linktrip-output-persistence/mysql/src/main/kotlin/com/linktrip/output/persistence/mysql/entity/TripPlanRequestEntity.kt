package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.trip.TripPlanRequest
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "trip_plan_request",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_trip_plan_request_member_task",
            columnNames = ["member_id", "video_analysis_task_id"],
        ),
    ],
    indexes = [
        Index(
            name = "idx_trip_plan_request_task_processed",
            columnList = "video_analysis_task_id, processed",
        ),
        Index(
            name = "idx_trip_plan_request_member_created",
            columnList = "member_id, created_at",
        ),
    ],
)
class TripPlanRequestEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "member_id", nullable = false, length = 36)
    val memberId: String,
    @Column(name = "video_analysis_task_id", nullable = false, length = 36)
    val videoAnalysisTaskId: String,
    @Column(name = "processed", nullable = false)
    var processed: Boolean = false,
) : BaseTimeEntity() {
    fun toDomain(): TripPlanRequest =
        TripPlanRequest(
            id = this.id,
            memberId = this.memberId,
            videoAnalysisTaskId = this.videoAnalysisTaskId,
            processed = this.processed,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(request: TripPlanRequest): TripPlanRequestEntity =
            TripPlanRequestEntity(
                id = request.id,
                memberId = request.memberId,
                videoAnalysisTaskId = request.videoAnalysisTaskId,
                processed = request.processed,
            )
    }
}
