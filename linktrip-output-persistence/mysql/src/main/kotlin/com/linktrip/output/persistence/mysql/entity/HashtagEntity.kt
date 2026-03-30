package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.Hashtag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "hashtag",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_hashtag_name",
            columnNames = ["name"],
        ),
    ],
)
class HashtagEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "name", nullable = false, length = 50)
    val name: String,
) : BaseTimeEntity() {
    fun toDomain(): Hashtag =
        Hashtag(
            id = this.id,
            name = this.name,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    companion object {
        fun from(hashtag: Hashtag): HashtagEntity =
            HashtagEntity(
                id = hashtag.id,
                name = hashtag.name,
            )
    }
}
