package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface YouTubeVideoJpaRepository : JpaRepository<YouTubeVideoEntity, String> {
    fun findAllByVideoIdIn(videoIds: List<String>): List<YouTubeVideoEntity>

    @Query("SELECT y.videoId FROM YouTubeVideoEntity y WHERE y.videoId IN :videoIds")
    fun findVideoIdsByVideoIdIn(
        @Param("videoIds") videoIds: List<String>,
    ): List<String>

    fun findAllByOrderByViewCountDesc(): List<YouTubeVideoEntity>

    fun findAllByCountryOrderByViewCountDesc(country: String): List<YouTubeVideoEntity>

    fun findAllByRegionOrderByViewCountDesc(region: String): List<YouTubeVideoEntity>

    @Query(
        """
        SELECT y FROM YouTubeVideoEntity y
        WHERE y.theme = :theme AND y.createdAt < :cursor
        ORDER BY y.createdAt DESC
        """,
    )
    fun findAllByThemeAndCreatedAtBefore(
        @Param("theme") theme: String,
        @Param("cursor") cursor: LocalDateTime,
        pageable: Pageable,
    ): List<YouTubeVideoEntity>

    @Query(
        """
        SELECT y FROM YouTubeVideoEntity y
        WHERE y.theme = :theme
        ORDER BY y.createdAt DESC
        """,
    )
    fun findAllByTheme(
        @Param("theme") theme: String,
        pageable: Pageable,
    ): List<YouTubeVideoEntity>
}
