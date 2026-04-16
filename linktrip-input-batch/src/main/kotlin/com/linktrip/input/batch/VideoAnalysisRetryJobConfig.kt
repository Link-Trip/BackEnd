package com.linktrip.input.batch

import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Configuration
class VideoAnalysisRetryJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort,
    private val videoAnalysisQueuePort: VideoAnalysisQueuePort,
) {
    @Bean
    fun videoAnalysisRetryJob(): Job =
        JobBuilder("videoAnalysisRetryJob", jobRepository)
            .start(videoAnalysisRetryStep())
            .build()

    @Bean
    fun videoAnalysisRetryStep(): Step =
        StepBuilder("videoAnalysisRetryStep", jobRepository)
            .tasklet(videoAnalysisRetryTasklet(), transactionManager)
            .build()

    @Bean
    fun videoAnalysisRetryTasklet(): Tasklet =
        Tasklet { _, _ ->
            val threshold = LocalDateTime.now().minusMinutes(STALE_THRESHOLD_MINUTES)
            val staleTasks =
                videoAnalysisTaskPersistencePort.findPendingTasks()
                    .filter { it.createdAt.isBefore(threshold) }

            if (staleTasks.isEmpty()) {
                logger.info { "재시도 대상 영상 분석 건 없음" }
                return@Tasklet RepeatStatus.FINISHED
            }

            val newTasks = staleTasks.filter { !videoAnalysisQueuePort.contains(it.id) }
            if (newTasks.isEmpty()) {
                logger.info { "재시도 대상 모두 큐에 이미 존재 (${staleTasks.size}건 스킵)" }
                return@Tasklet RepeatStatus.FINISHED
            }

            newTasks.forEach { task ->
                videoAnalysisQueuePort.enqueue(task.id, task.youtubeUrl)
            }
            logger.info {
                "방치된 PENDING 영상 분석 건 큐 재투입: ${newTasks.size}건 " +
                    "(${staleTasks.size - newTasks.size}건 이미 큐에 존재하여 스킵)"
            }

            RepeatStatus.FINISHED
        }

    companion object {
        private const val STALE_THRESHOLD_MINUTES = 5L
    }
}
