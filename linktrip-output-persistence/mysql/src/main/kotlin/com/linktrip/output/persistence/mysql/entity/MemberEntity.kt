package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.member.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_provider",
            columnNames = ["provider_type", "provider_id"],
        ),
    ],
    indexes = [
        Index(name = "idx_member_email", columnList = "email"),
    ],
)
class MemberEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "serial_number", nullable = false, length = 255)
    val serialNumber: String,
) : BaseTimeEntity() {
    fun toDomain(): Member =
        Member(
            id = this.id,
            serialNumber = this.serialNumber,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(member: Member): MemberEntity =
            MemberEntity(
                id = member.id,
                serialNumber = member.serialNumber,
            )
    }
}
