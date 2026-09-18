package com.linktrip.application.port.input

import com.linktrip.application.domain.feedback.FeedbackType
import com.linktrip.application.domain.member.Platform

interface FeedbackUseCase {
    fun create(command: CreateFeedbackCommand)

    data class CreateFeedbackCommand(
        val memberId: String,
        val type: FeedbackType,
        val content: String,
        val appVersion: String,
        val platform: Platform,
        val osVersion: String,
        val deviceModel: String,
    )
}
