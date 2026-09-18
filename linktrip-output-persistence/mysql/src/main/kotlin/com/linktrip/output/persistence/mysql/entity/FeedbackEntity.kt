package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.feedback.Feedback
import com.linktrip.application.domain.feedback.FeedbackType
import com.linktrip.application.domain.member.Platform
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "feedback",
    indexes = [
        Index(name = "idx_feedback_member_created", columnList = "member_id, created_at"),
    ],
)
class FeedbackEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "member_id", nullable = false, length = 36)
    val memberId: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: FeedbackType,
    @Column(name = "content", nullable = false, length = 200)
    val content: String,
    @Column(name = "app_version", nullable = false, length = 20)
    val appVersion: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    val platform: Platform,
    @Column(name = "os_version", nullable = false, length = 20)
    val osVersion: String,
    @Column(name = "device_model", nullable = false, length = 50)
    val deviceModel: String,
) : BaseTimeEntity() {
    fun toDomain(): Feedback =
        Feedback(
            id = this.id,
            memberId = this.memberId,
            type = this.type,
            content = this.content,
            appVersion = this.appVersion,
            platform = this.platform,
            osVersion = this.osVersion,
            deviceModel = this.deviceModel,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(feedback: Feedback): FeedbackEntity =
            FeedbackEntity(
                id = feedback.id,
                memberId = feedback.memberId,
                type = feedback.type,
                content = feedback.content,
                appVersion = feedback.appVersion,
                platform = feedback.platform,
                osVersion = feedback.osVersion,
                deviceModel = feedback.deviceModel,
            )
    }
}
