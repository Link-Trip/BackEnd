package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeChannelEntity
import com.linktrip.output.persistence.mysql.entity.YouTubeRecentVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeChannelJpaRepository
import com.linktrip.output.persistence.mysql.repository.YouTubeRecentVideoJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("youtubeChannelDbAdapter")
class YouTubeChannelPersistenceAdapter(
    private val youTubeChannelJpaRepository: YouTubeChannelJpaRepository,
    private val youTubeRecentVideoJpaRepository: YouTubeRecentVideoJpaRepository,
) : YouTubeChannelPersistencePort {
    @Transactional
    override fun saveAll(channels: List<YouTubeChannelDetail>) {
        val channelIds = channels.map { it.channelId }

        // 채널 upsert
        val existingMap =
            youTubeChannelJpaRepository.findAllByChannelIdIn(channelIds)
                .associateBy { it.channelId }

        val entitiesToSave =
            channels.map { detail ->
                existingMap[detail.channelId]?.apply { updateFrom(detail) }
                    ?: YouTubeChannelEntity.from(IdGenerator.generate(), detail)
            }
        youTubeChannelJpaRepository.saveAll(entitiesToSave)

        // 최신 영상: 기존 삭제 후 새로 저장
        youTubeRecentVideoJpaRepository.deleteAllByChannelIdIn(channelIds)

        val videoEntities =
            channels.flatMap { channel ->
                channel.recentVideos.map { video ->
                    YouTubeRecentVideoEntity.from(video)
                }
            }
        if (videoEntities.isNotEmpty()) {
            youTubeRecentVideoJpaRepository.saveAll(videoEntities)
        }
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeChannelDetail> {
        val channels = youTubeChannelJpaRepository.findAllByOrderBySubscriberCountDesc()
        if (channels.isEmpty()) return emptyList()

        // N+1 방지: 채널 ID 목록으로 한번에 조회 후 그룹핑
        val channelIds = channels.map { it.channelId }
        val recentVideosMap =
            youTubeRecentVideoJpaRepository.findAllByChannelIdIn(channelIds)
                .map { it.toDomain() }
                .groupBy { it.channelId }

        return channels.map { entity ->
            entity.toDomain(recentVideos = recentVideosMap[entity.channelId] ?: emptyList())
        }
    }
}
