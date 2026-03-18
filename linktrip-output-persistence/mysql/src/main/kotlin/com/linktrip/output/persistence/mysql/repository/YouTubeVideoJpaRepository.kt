package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface YouTubeVideoJpaRepository : JpaRepository<YouTubeVideoEntity, String> {
    fun findAllByVideoIdIn(videoIds: List<String>): List<YouTubeVideoEntity>

    @Query("SELECT y.videoId FROM YouTubeVideoEntity y WHERE y.videoId IN :videoIds")
    fun findVideoIdsByVideoIdIn(
        @Param("videoIds") videoIds: List<String>,
    ): List<String>

    fun findAllByOrderByViewCountDesc(): List<YouTubeVideoEntity>

    fun findAllByCountryOrderByViewCountDesc(country: String): List<YouTubeVideoEntity>

    fun findAllByRegionOrderByViewCountDesc(region: String): List<YouTubeVideoEntity>
}
