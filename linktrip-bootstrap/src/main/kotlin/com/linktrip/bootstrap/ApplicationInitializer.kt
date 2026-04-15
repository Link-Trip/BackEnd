package com.linktrip.bootstrap

import com.linktrip.application.domain.video.VideoAnalysisQueueConsumer
import com.linktrip.application.domain.youtube.SearchKeywordLoader
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import com.linktrip.output.http.properties.GcpProperties
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class ApplicationInitializer(
    private val gcpProperties: GcpProperties,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val videoAnalysisQueuePort: VideoAnalysisQueuePort,
    private val videoAnalysisQueueConsumer: VideoAnalysisQueueConsumer,
) {
    @PostConstruct
    fun init() {
        validateGcpCredentialsFile()
        loadSearchKeywords()
        reloadPendingAnalysisTasks()
        videoAnalysisQueueConsumer.startConsuming()
    }

    private fun validateGcpCredentialsFile() {
        val file = File(gcpProperties.credentialsPath)
        require(file.exists()) {
            "GCP credentials 파일이 존재하지 않습니다: ${gcpProperties.credentialsPath}"
        }
        logger.info { "GCP credentials 파일 확인 완료: ${gcpProperties.credentialsPath}" }
    }

    private fun loadSearchKeywords() {
        val startTime = System.currentTimeMillis()
        val keywords = SearchKeywordLoader.getAll()
        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "검색 키워드 초기화 완료: ${keywords.size}개, ${elapsed}ms" }
    }

    private fun reloadPendingAnalysisTasks() {
        val tasks = videoAnalysisTaskPersistencePort.findReloadableTasks()
        if (tasks.isEmpty()) {
            logger.info { "재적재할 미처리 영상 분석 건 없음" }
            return
        }

        tasks.forEach { task ->
            videoAnalysisQueuePort.enqueue(task.id, task.youtubeUrl)
        }
        logger.info { "미처리 영상 분석 건 큐 재적재 완료: ${tasks.size}건" }
    }
}
