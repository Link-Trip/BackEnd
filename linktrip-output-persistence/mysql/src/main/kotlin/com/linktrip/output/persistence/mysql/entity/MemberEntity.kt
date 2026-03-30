package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.member.Member
import com.linktrip.application.domain.member.ProviderType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    @Column(name = "email", length = 255)
    val email: String?,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    val providerType: ProviderType,
    @Column(name = "provider_id", nullable = false, length = 255)
    val providerId: String,
) : BaseTimeEntity() {
    fun toDomain(): Member =
        Member(
            id = this.id,
            email = this.email,
            providerType = this.providerType,
            providerId = this.providerId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(member: Member): MemberEntity =
            MemberEntity(
                id = member.id,
                email = member.email,
                providerType = member.providerType,
                providerId = member.providerId,
            )
    }
}
