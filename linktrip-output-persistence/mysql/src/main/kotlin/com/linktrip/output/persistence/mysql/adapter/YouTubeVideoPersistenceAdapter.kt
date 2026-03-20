package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeVideoQuerydslRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component("youtubeVideoDbAdapter")
class YouTubeVideoPersistenceAdapter(
    private val querydslRepository: YouTubeVideoQuerydslRepository,
    private val entityManager: EntityManager,
) : YouTubeVideoPersistencePort {
    @Transactional
    override fun saveAll(videos: List<YouTubeVideoMeta>) {
        val deduped = videos.associateBy { it.videoId }.values
        deduped.forEach { detail ->
            entityManager.persist(YouTubeVideoEntity.from(detail))
        }
    }

    @Transactional(readOnly = true)
    override fun findExistingVideoIds(videoIds: List<String>): Set<String> =
        querydslRepository.findVideoIdsByVideoIdIn(videoIds).toSet()

    @Transactional(readOnly = true)
    override fun findAll(): List<YouTubeVideoMeta> =
        querydslRepository.findAllOrderByViewCountDesc()
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByCountry(country: String): List<YouTubeVideoMeta> =
        querydslRepository.findAllByCountryOrderByViewCountDesc(country)
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByRegion(region: String): List<YouTubeVideoMeta> =
        querydslRepository.findAllByRegionOrderByViewCountDesc(region)
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta> {
        val pageable = PageRequest.of(0, size + 1)

        val entities =
            if (cursor != null) {
                querydslRepository.findAllByThemeAndCreatedAtBefore(theme, cursor, pageable)
            } else {
                querydslRepository.findAllByTheme(theme, pageable)
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
