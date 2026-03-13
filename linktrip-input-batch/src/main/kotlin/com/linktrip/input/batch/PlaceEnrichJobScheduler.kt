package com.linktrip.input.batch

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@EnableScheduling
@Component
class PlaceEnrichJobScheduler(
    private val jobLauncher: JobLauncher,
    private val placeEnrichRetryJob: Job,
) {
    @Scheduled(cron = "0 0 * * * *")
    fun run() {
        val params =
            JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()

        logger.info { "장소 보강 리트라이 Job 실행" }
        jobLauncher.run(placeEnrichRetryJob, params)
    }
}
