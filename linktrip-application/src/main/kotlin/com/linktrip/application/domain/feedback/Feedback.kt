package com.linktrip.application.domain.feedback

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.application.domain.member.Platform
import com.linktrip.application.port.input.FeedbackUseCase.CreateFeedbackCommand
import java.time.LocalDateTime

data class Feedback(
    val id: String,
    val memberId: String,
    val type: FeedbackType,
    val content: String,
    val appVersion: String,
    val platform: Platform,
    val osVersion: String,
    val deviceModel: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(command: CreateFeedbackCommand): Feedback =
            Feedback(
                id = IdGenerator.generate(),
                memberId = command.memberId,
                type = command.type,
                content = command.content.trim(),
                appVersion = command.appVersion,
                platform = command.platform,
                osVersion = command.osVersion,
                deviceModel = command.deviceModel,
            )
    }
}
