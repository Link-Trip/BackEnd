package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.port.input.TermsUseCase.TermsWithAgreement
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "약관 정보")
data class TermsResponse(
    @field:Schema(description = "약관 유형", example = "SERVICE", allowableValues = ["SERVICE", "PRIVACY"])
    val type: String,
    @field:Schema(description = "약관 제목", example = "서비스 이용약관 동의")
    val title: String,
    @field:Schema(description = "필수 여부", example = "true")
    val required: Boolean,
    @field:Schema(description = "약관 버전", example = "1")
    val version: Int,
    @field:Schema(description = "약관 전문 URL (웹뷰로 노출)", example = "https://pingo.notion.site/terms")
    val detailUrl: String,
    @field:Schema(description = "현재 버전 동의 여부 (false면 동의 시트 노출 필요)", example = "false")
    val agreed: Boolean,
) {
    companion object {
        fun from(termsWithAgreement: TermsWithAgreement): TermsResponse =
            TermsResponse(
                type = termsWithAgreement.terms.type.name,
                title = termsWithAgreement.terms.title,
                required = termsWithAgreement.terms.required,
                version = termsWithAgreement.terms.version,
                detailUrl = termsWithAgreement.terms.detailUrl,
                agreed = termsWithAgreement.agreed,
            )
    }
}

@Schema(description = "약관 목록 응답")
data class TermsResponses(
    @field:Schema(description = "현재 노출 대상 약관 목록")
    val terms: List<TermsResponse>,
) {
    companion object {
        fun from(termsWithAgreements: List<TermsWithAgreement>): TermsResponses =
            TermsResponses(
                terms = termsWithAgreements.map { TermsResponse.from(it) },
            )
    }
}
