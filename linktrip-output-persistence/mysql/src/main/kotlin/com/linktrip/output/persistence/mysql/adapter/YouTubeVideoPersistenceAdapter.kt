package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeVideoJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component("youtubeVideoDbAdapter")
class YouTubeVideoPersistenceAdapter(
    private val youTubeVideoJpaRepository: YouTubeVideoJpaRepository,
    private val entityManager: EntityManager,
) : YouTubeVideoPersistencePort {
    @Transactional
    override fun saveAll(videos: List<YouTubeVideoDetail>) {
        val deduped = videos.associateBy { it.videoId }.values
        deduped.forEach { detail ->
            entityManager.persist(YouTubeVideoEntity.from(detail))
        }
    }

    @Transactional(readOnly = true)
    override fun findExistingVideoIds(videoIds: List<String>): Set<String> =
        youTubeVideoJpaRepository.findVideoIdsByVideoIdIn(videoIds).toSet()

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeVideoDetail> =
        youTubeVideoJpaRepository.findAllByOrderByViewCountDesc()
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByCountry(country: String): List<YouTubeVideoDetail> =
        youTubeVideoJpaRepository.findAllByCountryOrderByViewCountDesc(country)
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByRegion(region: String): List<YouTubeVideoDetail> =
        youTubeVideoJpaRepository.findAllByRegionOrderByViewCountDesc(region)
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoDetail> {
        val pageable = PageRequest.of(0, size + 1)

        val entities =
            if (cursor != null) {
                youTubeVideoJpaRepository.findAllByThemeAndCreatedAtBefore(theme, cursor, pageable)
            } else {
                youTubeVideoJpaRepository.findAllByTheme(theme, pageable)
            }

        val hasNext = entities.size > size
        val items = entities.take(size).map { it.toDomain() }
        val nextCursor = if (hasNext) entities[size - 1].createdAt.toString() else null

        return CursorPage(
            items = items,
            nextCursor = nextCursor,
            hasNext = hasNext,
        )
    }
}
