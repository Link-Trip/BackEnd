package com.linktrip.application.infra.queue

import com.linktrip.application.domain.video.VideoAnalyzeEvent
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue

private val logger = KotlinLogging.logger {}

@Component
class InMemoryVideoAnalysisQueueAdapter : VideoAnalysisQueuePort {
    private val queue = LinkedBlockingQueue<VideoAnalyzeEvent>()

    override fun enqueue(
        videoAnalysisTaskId: String,
        youtubeUrl: String,
    ) {
        val event = VideoAnalyzeEvent(videoAnalysisTaskId, youtubeUrl)
        queue.put(event)
        logger.info { "영상 분석 큐 추가: taskId=$videoAnalysisTaskId (큐 크기: ${queue.size})" }
    }

    override fun dequeue(): VideoAnalyzeEvent? =
        try {
            queue.take()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }

    override fun size(): Int = queue.size

    override fun contains(videoAnalysisTaskId: String): Boolean =
        queue.any { it.videoAnalysisTaskId == videoAnalysisTaskId }
}
