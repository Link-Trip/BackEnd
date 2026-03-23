package com.linktrip.output.http.adapter

import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LoggingVideoAnalysisNotificationAdapter : VideoAnalysisNotificationPort {
    @Async("NotificationExecutor")
    override fun notifyAnalysisComplete(videoAnalysisTaskId: String) {
        logger.info { "영상 분석 완료 알림: videoAnalysisTaskId=$videoAnalysisTaskId (장소 보강 포함)" }
    }
}
