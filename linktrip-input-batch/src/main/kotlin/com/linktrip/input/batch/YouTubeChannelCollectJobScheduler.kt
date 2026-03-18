package com.linktrip.input.batch

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class YouTubeChannelCollectJobScheduler(
    private val jobLauncher: JobLauncher,
    @param:Qualifier("youTubeChannelCollectJob")
    private val youTubeChannelCollectJob: Job,
) {
    // TODO: 테스트 완료 후 스케줄 활성화
    // @Scheduled(cron = "0 0 */6 * * *")
    fun run() {
        val params =
            JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()

        logger.info { "YouTube 채널 수집 Job 실행" }
        try {
            val execution = jobLauncher.run(youTubeChannelCollectJob, params)
            if (execution.status.isUnsuccessful) {
                logger.error {
                    "YouTube 채널 수집 Job 실패: status=${execution.status}, " +
                        "exitStatus=${execution.exitStatus}"
                }
            } else {
                logger.info { "YouTube 채널 수집 Job 완료: status=${execution.status}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "YouTube 채널 수집 Job 실패" }
        }
    }
}
