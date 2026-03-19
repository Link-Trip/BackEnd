package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.YouTubeRecentVideoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface YouTubeRecentVideoJpaRepository : JpaRepository<YouTubeRecentVideoEntity, String>
