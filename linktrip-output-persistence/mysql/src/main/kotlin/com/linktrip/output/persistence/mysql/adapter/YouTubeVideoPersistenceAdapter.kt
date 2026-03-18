package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import com.linktrip.output.persistence.mysql.repository.YouTubeVideoJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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
}
