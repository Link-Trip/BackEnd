package com.linktrip.common.config.event

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventsConfiguration(
    private val applicationContext: ApplicationContext,
) {
    @Bean
    fun eventInitializer(): InitializingBean =
        InitializingBean {
            Events.setPublisher(applicationContext)
        }
}
