package com.linktrip.output.cache.caffeine.adapter

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalyzeEvent
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

/**
 * 우선순위 + FIFO in-memory 영상 분석 큐.
 *
 * 정렬 기준 (먼저 처리되는 순서):
 * 1. [Source.priority] 작은 순 (USER 가 BATCH 보다 먼저)
 * 2. enqueue 시퀀스 작은 순 (동일 priority 안에서는 FIFO)
 *
 * 시퀀스는 [AtomicLong] 으로 단조 증가 → 같은 source 끼리 들어온 순서가 보장된다.
 * (PriorityBlockingQueue 자체는 동일 우선순위 요소 순서를 보장하지 않으므로 tiebreaker 필요)
 */
@Component
class InMemoryVideoAnalysisQueueAdapter : VideoAnalysisQueuePort {
    private data class QueueEntry(val event: VideoAnalyzeEvent, val sequence: Long)

    private val sequence = AtomicLong(0)

    private val queue =
        PriorityBlockingQueue<QueueEntry>(
            INITIAL_CAPACITY,
            compareBy({ it.event.source.priority }, { it.sequence }),
        )

    override fun enqueue(
        videoAnalysisTaskId: String,
        youtubeUrl: String,
        source: Source,
    ) {
        val event = VideoAnalyzeEvent(videoAnalysisTaskId, youtubeUrl, source)
        val entry = QueueEntry(event, sequence.incrementAndGet())
        queue.put(entry)
        logger.info {
            "영상 분석 큐 추가: taskId=$videoAnalysisTaskId, source=$source (큐 크기: ${queue.size})"
        }
    }

    override fun dequeue(): VideoAnalyzeEvent? =
        try {
            queue.take().event
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }

    override fun size(): Int = queue.size

    override fun contains(videoAnalysisTaskId: String): Boolean = queue.any { it.event.videoAnalysisTaskId == videoAnalysisTaskId }

    companion object {
        private const val INITIAL_CAPACITY = 16
    }
}
