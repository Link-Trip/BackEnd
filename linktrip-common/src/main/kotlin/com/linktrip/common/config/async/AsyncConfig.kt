package com.linktrip.common.config.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig : DisposableBean {
    @Bean(name = ["AccessLogExecutor"])
    fun accessLogExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 2
            queueCapacity = 500
            setThreadNamePrefix("AsyncAccessLog-")
            initialize()
        }

    @Bean(name = ["NotificationExecutor"])
    fun notificationExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 2
            queueCapacity = 100
            setThreadNamePrefix("AsyncNotification-")
            initialize()
        }

    @Bean(name = ["VideoAnalyzeExecutor"])
    fun videoAnalyzeExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 5
            queueCapacity = 20
            setThreadNamePrefix("AsyncVideoAnalyze-")
            initialize()
        }

    private lateinit var placeEnrichTaskExecutor: ThreadPoolTaskExecutor

    @Bean
    fun placeEnrichDispatcher(): CoroutineDispatcher {
        placeEnrichTaskExecutor =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 5
                maxPoolSize = 10
                queueCapacity = 50
                setThreadNamePrefix("PlaceEnrich-")
                initialize()
            }
        return placeEnrichTaskExecutor.asCoroutineDispatcher()
    }

    override fun destroy() {
        if (::placeEnrichTaskExecutor.isInitialized) {
            placeEnrichTaskExecutor.shutdown()
        }
    }
}
