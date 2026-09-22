package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.terms.Terms
import com.linktrip.application.domain.terms.TermsType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "terms",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_terms_type_version", columnNames = ["type", "version"]),
    ],
)
class TermsEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    val type: TermsType,
    @Column(name = "title", nullable = false, length = 100)
    val title: String,
    @Column(name = "required", nullable = false)
    val required: Boolean,
    @Column(name = "version", nullable = false)
    val version: Int,
    @Column(name = "detail_url", nullable = false, length = 512)
    var detailUrl: String,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
) : BaseTimeEntity() {
    fun toDomain(): Terms =
        Terms(
            id = this.id,
            type = this.type,
            title = this.title,
            required = this.required,
            version = this.version,
            detailUrl = this.detailUrl,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
