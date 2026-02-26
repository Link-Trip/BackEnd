package com.linktrip.common.config.event

import org.springframework.context.ApplicationEventPublisher

object Events {
    private var publisher: ApplicationEventPublisher? = null

    fun setPublisher(publisher: ApplicationEventPublisher) {
        this.publisher = publisher
    }

    fun raise(event: Any) {
        publisher?.publishEvent(event)
    }
}
