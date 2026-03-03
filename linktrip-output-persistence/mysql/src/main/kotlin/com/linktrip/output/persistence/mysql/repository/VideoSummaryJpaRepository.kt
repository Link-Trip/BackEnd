package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.VideoSummaryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoSummaryJpaRepository : JpaRepository<VideoSummaryEntity, String> {
    fun findByYoutubeUrl(youtubeUrl: String): VideoSummaryEntity?
}
