package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.video.Hashtag
import com.linktrip.application.domain.video.VideoAnalysisTaskHashtag
import com.linktrip.application.port.output.persistence.HashtagPersistencePort
import com.linktrip.application.port.output.persistence.TaskHashtagMapping
import com.linktrip.output.persistence.mysql.entity.HashtagEntity
import com.linktrip.output.persistence.mysql.entity.VideoAnalysisTaskHashtagEntity
import com.linktrip.output.persistence.mysql.repository.HashtagJpaRepository
import com.linktrip.output.persistence.mysql.repository.VideoAnalysisTaskHashtagJpaRepository
import com.linktrip.output.persistence.mysql.repository.VideoAnalysisTaskHashtagQuerydslRepository
import org.springframework.stereotype.Component

@Component
class HashtagPersistenceAdapter(
    private val hashtagJpaRepository: HashtagJpaRepository,
    private val taskHashtagJpaRepository: VideoAnalysisTaskHashtagJpaRepository,
    private val taskHashtagQuerydslRepository: VideoAnalysisTaskHashtagQuerydslRepository,
) : HashtagPersistencePort {
    override fun findByNames(names: List<String>): List<Hashtag> =
        hashtagJpaRepository.findByNameIn(names).map { it.toDomain() }

    override fun saveAll(hashtags: List<Hashtag>): List<Hashtag> =
        hashtagJpaRepository.saveAll(hashtags.map { HashtagEntity.from(it) }).map { it.toDomain() }

    override fun saveAllTaskHashtags(taskHashtags: List<VideoAnalysisTaskHashtag>) {
        taskHashtagJpaRepository.saveAll(taskHashtags.map { VideoAnalysisTaskHashtagEntity.from(it) })
    }

    override fun findHashtagNamesByVideoAnalysisTaskIds(videoAnalysisTaskIds: List<String>): List<TaskHashtagMapping> =
        taskHashtagQuerydslRepository.findByVideoAnalysisTaskIds(videoAnalysisTaskIds).map { row ->
            TaskHashtagMapping(
                videoAnalysisTaskId = row.videoAnalysisTaskId,
                hashtagName = row.hashtagName,
            )
        }
}
