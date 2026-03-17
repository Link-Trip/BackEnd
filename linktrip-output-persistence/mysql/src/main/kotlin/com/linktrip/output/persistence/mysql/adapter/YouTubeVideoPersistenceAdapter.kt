package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeVideoJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("youtubeVideoDbAdapter")
class YouTubeVideoPersistenceAdapter(
    private val youTubeVideoJpaRepository: YouTubeVideoJpaRepository,
) : YouTubeVideoPersistencePort {
    @Transactional
    override fun saveAll(videos: List<YouTubeVideoDetail>) {
        val videoIds = videos.map { it.videoId }
        val existingMap =
            youTubeVideoJpaRepository.findAllByVideoIdIn(videoIds)
                .associateBy { it.videoId }

        val entitiesToSave =
            videos.map { detail ->
                existingMap[detail.videoId]?.apply { updateFrom(detail) }
                    ?: YouTubeVideoEntity.from(IdGenerator.generate(), detail)
            }

        youTubeVideoJpaRepository.saveAll(entitiesToSave)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeVideoDetail> =
        youTubeVideoJpaRepository.findAllByOrderByViewCountDesc()
            .map { it.toDomain() }
}
