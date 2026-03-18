package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeChannelEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeChannelJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("youtubeChannelDbAdapter")
class YouTubeChannelPersistenceAdapter(
    private val youTubeChannelJpaRepository: YouTubeChannelJpaRepository,
) : YouTubeChannelPersistencePort {
    @Transactional
    override fun saveAll(channels: List<YouTubeChannelDetail>) {
        val channelIds = channels.map { it.channelId }
        val existingMap =
            youTubeChannelJpaRepository.findAllByChannelIdIn(channelIds)
                .associateBy { it.channelId }

        val entitiesToSave =
            channels.map { detail ->
                existingMap[detail.channelId]?.apply { updateFrom(detail) }
                    ?: YouTubeChannelEntity.from(IdGenerator.generate(), detail)
            }

        youTubeChannelJpaRepository.saveAll(entitiesToSave)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeChannelDetail> =
        youTubeChannelJpaRepository.findAllByOrderBySubscriberCountDesc()
            .map { it.toDomain() }
}
