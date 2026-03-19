package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeChannelEntity
import com.linktrip.output.persistence.mysql.entity.YouTubeRecentVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeChannelJpaRepository
import com.linktrip.output.persistence.mysql.repository.YouTubeChannelQuerydslRepository
import com.linktrip.output.persistence.mysql.repository.YouTubeRecentVideoJpaRepository
import com.linktrip.output.persistence.mysql.repository.YouTubeRecentVideoQuerydslRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("youtubeChannelDbAdapter")
class YouTubeChannelPersistenceAdapter(
    private val channelJpaRepository: YouTubeChannelJpaRepository,
    private val channelQuerydslRepository: YouTubeChannelQuerydslRepository,
    private val recentVideoJpaRepository: YouTubeRecentVideoJpaRepository,
    private val recentVideoQuerydslRepository: YouTubeRecentVideoQuerydslRepository,
) : YouTubeChannelPersistencePort {
    @Transactional
    override fun saveAll(channels: List<YouTubeChannelDetail>) {
        if (channels.isEmpty()) return

        val channelIds = channels.map { it.channelId }

        // 채널 upsert
        val existingMap =
            channelQuerydslRepository.findAllByChannelIdIn(channelIds)
                .associateBy { it.channelId }

        val entitiesToSave =
            channels.map { detail ->
                existingMap[detail.channelId]?.apply { updateFrom(detail) }
                    ?: YouTubeChannelEntity.from(IdGenerator.generate(), detail)
            }
        channelJpaRepository.saveAll(entitiesToSave)

        // 최신 영상: 기존 삭제 후 새로 저장
        recentVideoQuerydslRepository.deleteAllByChannelIdIn(channelIds)

        val videoEntities =
            channels.flatMap { channel ->
                channel.recentVideos.map { video ->
                    YouTubeRecentVideoEntity.from(video)
                }
            }
        if (videoEntities.isNotEmpty()) {
            recentVideoJpaRepository.saveAll(videoEntities)
        }
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeChannelDetail> {
        val channels = channelQuerydslRepository.findAllOrderBySubscriberCountDesc()
        if (channels.isEmpty()) return emptyList()

        // N+1 방지: 채널 ID 목록으로 한번에 조회 후 그룹핑
        val channelIds = channels.map { it.channelId }
        val recentVideosMap =
            recentVideoQuerydslRepository.findAllByChannelIdIn(channelIds)
                .map { it.toDomain() }
                .groupBy { it.channelId }
                .mapValues { (_, videos) ->
                    videos.sortedByDescending { it.publishedAt }
                }

        return channels.map { entity ->
            entity.toDomain(recentVideos = recentVideosMap[entity.channelId].orEmpty())
        }
    }
}
