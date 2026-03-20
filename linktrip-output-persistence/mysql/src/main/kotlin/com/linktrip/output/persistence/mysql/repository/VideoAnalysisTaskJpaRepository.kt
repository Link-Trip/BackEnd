package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.VideoAnalysisTaskEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoAnalysisTaskJpaRepository : JpaRepository<VideoAnalysisTaskEntity, String> {
    fun findByYoutubeUrl(youtubeUrl: String): VideoAnalysisTaskEntity?
}
