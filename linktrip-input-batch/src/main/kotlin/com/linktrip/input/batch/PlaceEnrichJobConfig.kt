package com.linktrip.input.batch

import com.linktrip.application.domain.video.PlaceEnrichService
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

@Configuration
class PlaceEnrichJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val placeEnrichService: PlaceEnrichService,
) {
    @Bean
    fun placeEnrichRetryJob(): Job =
        JobBuilder("placeEnrichRetryJob", jobRepository)
            .start(placeEnrichRetryStep())
            .build()

    @Bean
    fun placeEnrichRetryStep(): Step =
        StepBuilder("placeEnrichRetryStep", jobRepository)
            .tasklet(placeEnrichRetryTasklet(), transactionManager)
            .build()

    @Bean
    fun placeEnrichRetryTasklet(): Tasklet =
        Tasklet { _, _ ->
            placeEnrichService.retryAll()
            RepeatStatus.FINISHED
        }
}
