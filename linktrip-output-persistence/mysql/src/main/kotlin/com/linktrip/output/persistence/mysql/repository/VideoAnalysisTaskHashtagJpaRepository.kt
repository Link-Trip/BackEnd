package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.VideoAnalysisTaskHashtagEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoAnalysisTaskHashtagJpaRepository : JpaRepository<VideoAnalysisTaskHashtagEntity, String>
