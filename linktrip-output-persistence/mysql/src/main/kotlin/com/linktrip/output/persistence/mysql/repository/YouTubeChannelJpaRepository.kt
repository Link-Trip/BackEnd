package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.YouTubeChannelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface YouTubeChannelJpaRepository : JpaRepository<YouTubeChannelEntity, String>
