package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface YouTubeVideoJpaRepository : JpaRepository<YouTubeVideoEntity, String>
