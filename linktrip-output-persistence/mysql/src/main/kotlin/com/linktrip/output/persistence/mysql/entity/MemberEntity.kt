package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.member.Member
import com.linktrip.application.domain.member.Platform
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_member_serial_number", columnNames = ["serial_number"]),
    ],
)
class MemberEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "serial_number", nullable = false, length = 255)
    val serialNumber: String,
    @Column(name = "fcm_token", length = 512)
    var fcmToken: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20)
    var platform: Platform? = null,
    @Column(name = "notification_enabled", nullable = false)
    var notificationEnabled: Boolean = true,
) : BaseTimeEntity() {
    fun applySettings(member: Member) {
        this.fcmToken = member.fcmToken
        this.platform = member.platform
        this.notificationEnabled = member.notificationEnabled
    }

    fun toDomain(): Member =
        Member(
            id = this.id,
            serialNumber = this.serialNumber,
            fcmToken = this.fcmToken,
            platform = this.platform,
            notificationEnabled = this.notificationEnabled,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(member: Member): MemberEntity =
            MemberEntity(
                id = member.id,
                serialNumber = member.serialNumber,
                fcmToken = member.fcmToken,
                platform = member.platform,
                notificationEnabled = member.notificationEnabled,
            )
    }
}
