package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.video.Hashtag
import com.linktrip.application.domain.video.VideoAnalysisTaskHashtag

interface HashtagPersistencePort {
    fun findByNames(names: List<String>): List<Hashtag>

    fun saveAll(hashtags: List<Hashtag>): List<Hashtag>

    fun saveAllTaskHashtags(taskHashtags: List<VideoAnalysisTaskHashtag>)

    fun findHashtagNamesByVideoAnalysisTaskIds(videoAnalysisTaskIds: List<String>): List<TaskHashtagMapping>
}

data class TaskHashtagMapping(
    val videoAnalysisTaskId: String,
    val hashtagName: String,
)
