package com.linktrip.output.persistence.mysql.adapter

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
) : VideoSummaryPersistencePort {
    override fun save(videoSummary: VideoSummary): VideoSummary {
        val entity = VideoSummaryEntity.from(videoSummary)
        return videoSummaryJpaRepository.save(entity).toDomain()
    }

    override fun findByYoutubeUrl(youtubeUrl: String): VideoSummary? =
        videoSummaryJpaRepository.findByYoutubeUrl(youtubeUrl)?.toDomain()

    override fun findById(id: String): VideoSummary? = videoSummaryJpaRepository.findById(id).orElse(null)?.toDomain()

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

    @Transactional
    override fun updateValidAndStatus(
        id: String,
        valid: Boolean,
        status: VideoSummaryStatus,
    ) {
        val entity =
            videoSummaryJpaRepository.findById(id).orElseThrow {
                IllegalArgumentException("VideoSummary not found: id=$id")
            }
        entity.valid = valid
        entity.status = status
    }
}
