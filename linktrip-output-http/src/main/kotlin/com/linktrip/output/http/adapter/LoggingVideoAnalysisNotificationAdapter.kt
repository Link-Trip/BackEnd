package com.linktrip.output.http.adapter

import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LoggingVideoAnalysisNotificationAdapter : VideoAnalysisNotificationPort {
    override fun notifyAnalysisComplete(videoSummaryId: String) {
        logger.info { "영상 분석 완료 알림: videoSummaryId=$videoSummaryId (장소 보강 포함)" }
    }
}
