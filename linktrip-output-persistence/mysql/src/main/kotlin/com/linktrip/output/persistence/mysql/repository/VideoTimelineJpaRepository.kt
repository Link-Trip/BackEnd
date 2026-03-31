package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.VideoTimelineEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoTimelineJpaRepository : JpaRepository<VideoTimelineEntity, String>
