package com.linktrip.output.persistence.mysql.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.linktrip.application.domain.video.VideoAnalysisResult
import com.linktrip.application.domain.video.VideoSummary
import com.linktrip.application.domain.video.VideoSummaryStatus
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import com.linktrip.output.persistence.mysql.entity.VideoSummaryEntity
import com.linktrip.output.persistence.mysql.repository.VideoSummaryJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VideoSummaryAdapter(
    private val videoSummaryJpaRepository: VideoSummaryJpaRepository,
    private val objectMapper: ObjectMapper,
) : VideoSummaryPersistencePort {
    override fun save(videoSummary: VideoSummary): VideoSummary {
        val entity =
            VideoSummaryEntity(
                id = videoSummary.id,
                youtubeUrl = videoSummary.youtubeUrl,
                summary = videoSummary.summary?.let { objectMapper.writeValueAsString(it) },
                status = videoSummary.status,
            )
        return videoSummaryJpaRepository.save(entity).toDomain()
    }

    override fun findByYoutubeUrl(youtubeUrl: String): VideoSummary? =
        videoSummaryJpaRepository.findByYoutubeUrl(youtubeUrl)?.toDomain()

    override fun findById(id: String): VideoSummary? = videoSummaryJpaRepository.findById(id).orElse(null)?.toDomain()

    @Transactional
    override fun updateSummaryAndStatus(
        id: String,
        summary: VideoAnalysisResult,
        status: VideoSummaryStatus,
    ) {
        val entity =
            videoSummaryJpaRepository.findById(id).orElseThrow {
                IllegalArgumentException("VideoSummary not found: id=$id")
            }
        entity.summary = objectMapper.writeValueAsString(summary)
        entity.status = status
    }

    @Transactional
    override fun updateStatus(
        id: String,
        status: VideoSummaryStatus,
    ) {
        val entity =
            videoSummaryJpaRepository.findById(id).orElseThrow {
                IllegalArgumentException("VideoSummary not found: id=$id")
            }
        entity.status = status
    }

    private fun VideoSummaryEntity.toDomain(): VideoSummary =
        VideoSummary(
            id = this.id,
            youtubeUrl = this.youtubeUrl,
            summary = this.summary?.let { objectMapper.readValue(it, VideoAnalysisResult::class.java) },
            status = this.status,
        )
}
