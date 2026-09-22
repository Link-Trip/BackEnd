package com.linktrip.input.http.controller

import com.linktrip.application.domain.terms.TermsType
import com.linktrip.application.port.input.TermsUseCase
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.controller.docs.TermsDocs
import com.linktrip.input.http.controller.dto.request.AgreeTermsRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.TermsResponses
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/terms")
class TermsController(
    private val termsUseCase: TermsUseCase,
) : TermsDocs {
    @GetMapping
    override fun getTerms(
        @AuthenticatedMember memberId: String,
    ): ApiResponse<TermsResponses> {
        val terms = termsUseCase.getTerms(memberId)
        return ApiResponse.ok(TermsResponses.from(terms))
    }

    @PostMapping("/agreement")
    override fun agree(
        @AuthenticatedMember memberId: String,
        @Validated @RequestBody request: AgreeTermsRequest,
    ): ApiResponse<Unit> {
        termsUseCase.agree(
            memberId = memberId,
            types = request.types.map { TermsType.from(it) },
        )
        return ApiResponse.ok()
    }
}
