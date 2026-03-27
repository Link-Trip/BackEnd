package com.linktrip.application.port.output.external

interface VideoAnalysisNotificationPort {
    fun notifyAnalysisComplete(
        videoAnalysisTaskId: String,
        memberIds: List<String>,
    )
}
