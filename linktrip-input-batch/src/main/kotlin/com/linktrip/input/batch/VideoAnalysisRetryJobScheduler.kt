package com.linktrip.input.batch

import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class VideoAnalysisRetryJobScheduler(
    private val jobLauncher: JobLauncher,
    private val videoAnalysisRetryJob: Job,
    private val videoAnalysisQueuePort: VideoAnalysisQueuePort,
) {
    @Scheduled(cron = "0 */5 * * * *")
    fun run() {
        val params =
            JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()

        logger.info { "영상 분석 재시도 Job 실행" }
        try {
            val execution = jobLauncher.run(videoAnalysisRetryJob, params)
            if (execution.status.isUnsuccessful) {
                logger.error {
                    "영상 분석 재시도 Job 실패: status=${execution.status}"
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "영상 분석 재시도 Job 실패" }
        }
    }

    @Scheduled(fixedDelay = 10_000)
    fun logQueueSize() {
        logger.info { "영상 분석 큐 사이즈: ${videoAnalysisQueuePort.size()}" }
    }
}
