package com.linktrip.input.batch

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class YouTubeCollectJobScheduler(
    private val jobLauncher: JobLauncher,
    private val youTubeCollectJob: Job,
) {
    // TODO: 좋아요/조회수 등 메타데이터 최신화가 필요해질 경우, 별도 메타데이터 갱신 Job을 추가로 작성할 것
    @Scheduled(cron = "0 0 * * * *")
    fun run() {
        val params =
            JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()

        logger.info { "YouTube 영상 수집 Job 실행" }
        try {
            val execution = jobLauncher.run(youTubeCollectJob, params)
            if (execution.status.isUnsuccessful) {
                logger.error { "YouTube 영상 수집 Job 실패: status=${execution.status}, exitStatus=${execution.exitStatus}" }
            } else {
                logger.info { "YouTube 영상 수집 Job 완료: status=${execution.status}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "YouTube 영상 수집 Job 실패" }
        }
    }
}
