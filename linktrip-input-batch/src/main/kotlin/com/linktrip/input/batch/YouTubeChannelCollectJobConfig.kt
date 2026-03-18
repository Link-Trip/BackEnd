package com.linktrip.input.batch

import com.linktrip.application.domain.youtube.YouTubeChannelCollectService
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
class YouTubeChannelCollectJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val youTubeChannelCollectService: YouTubeChannelCollectService,
) {
    @Bean
    fun youTubeChannelCollectJob(): Job =
        JobBuilder("youTubeChannelCollectJob", jobRepository)
            .start(youTubeChannelCollectStep())
            .build()

    @Bean
    fun youTubeChannelCollectStep(): Step =
        StepBuilder("youTubeChannelCollectStep", jobRepository)
            .tasklet(youTubeChannelCollectTasklet(), transactionManager)
            .build()

    @Bean
    fun youTubeChannelCollectTasklet(): Tasklet =
        Tasklet { _, _ ->
            youTubeChannelCollectService.collectChannels()
            RepeatStatus.FINISHED
        }
}
