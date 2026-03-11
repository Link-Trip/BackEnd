package com.linktrip.application.port.output.external

interface VideoAnalysisNotificationPort {
    fun notifyAnalysisComplete(videoSummaryId: String)
}
