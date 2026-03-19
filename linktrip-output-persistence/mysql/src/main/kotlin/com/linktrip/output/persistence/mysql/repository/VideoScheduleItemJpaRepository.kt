package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.VideoScheduleItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoScheduleItemJpaRepository : JpaRepository<VideoScheduleItemEntity, String>
