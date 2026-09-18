package com.linktrip.input.http.controller

import com.linktrip.application.domain.feedback.FeedbackType
import com.linktrip.application.domain.member.Platform
import com.linktrip.application.port.input.FeedbackUseCase
import com.linktrip.application.port.input.FeedbackUseCase.CreateFeedbackCommand
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.controller.docs.FeedbackDocs
import com.linktrip.input.http.controller.dto.request.CreateFeedbackRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/feedback")
class FeedbackController(
    private val feedbackUseCase: FeedbackUseCase,
) : FeedbackDocs {
    @PostMapping
    override fun createFeedback(
        @AuthenticatedMember memberId: String,
        @Validated @RequestBody request: CreateFeedbackRequest,
    ): ApiResponse<Unit> {
        feedbackUseCase.create(
            CreateFeedbackCommand(
                memberId = memberId,
                type = FeedbackType.from(request.type),
                content = request.content,
                appVersion = request.appVersion,
                platform = Platform.from(request.platform),
                osVersion = request.osVersion,
                deviceModel = request.deviceModel,
            ),
        )
        return ApiResponse.ok()
    }
}
