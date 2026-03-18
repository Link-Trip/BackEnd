package com.linktrip.input.batch

import com.linktrip.application.domain.youtube.YouTubeCollectService
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
class YouTubeCollectJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val youTubeCollectService: YouTubeCollectService,
) {
    @Bean
    fun youTubeCollectJob(): Job =
        JobBuilder("youTubeCollectJob", jobRepository)
            .start(youTubeCollectStep())
            .build()

    @Bean
    fun youTubeCollectStep(): Step =
        StepBuilder("youTubeCollectStep", jobRepository)
            .tasklet(youTubeCollectTasklet(), transactionManager)
            .build()

    @Bean
    fun youTubeCollectTasklet(): Tasklet =
        Tasklet { _, _ ->
            youTubeCollectService.collectVideos()
            RepeatStatus.FINISHED
        }
}
