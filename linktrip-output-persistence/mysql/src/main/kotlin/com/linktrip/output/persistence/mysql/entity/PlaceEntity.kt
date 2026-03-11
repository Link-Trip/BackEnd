package com.linktrip.output.persistence.mysql.entity

import com.linktrip.application.domain.video.Place
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "place",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_place_google_place_id", columnNames = ["google_place_id"]),
    ],
    indexes = [
        Index(name = "idx_place_google_place_id", columnList = "google_place_id"),
    ],
)
class PlaceEntity(
    @Id
    @Column(length = 36)
    val id: String,
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    @Column(name = "google_place_id", nullable = false, length = 255)
    val googlePlaceId: String,
    @Column(name = "address", length = 500)
    val address: String? = null,
    @Column(name = "latitude")
    val latitude: Double? = null,
    @Column(name = "longitude")
    val longitude: Double? = null,
) : BaseTimeEntity() {
    fun toDomain(): Place =
        Place(
            id = this.id,
            name = this.name,
            googlePlaceId = this.googlePlaceId,
            address = this.address,
            latitude = this.latitude,
            longitude = this.longitude,
        )

    companion object {
        fun from(place: Place): PlaceEntity =
            PlaceEntity(
                id = place.id,
                name = place.name,
                googlePlaceId = place.googlePlaceId,
                address = place.address,
                latitude = place.latitude,
                longitude = place.longitude,
            )
    }
}
