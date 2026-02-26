package com.linktrip.common.config.async

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig {
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
}
