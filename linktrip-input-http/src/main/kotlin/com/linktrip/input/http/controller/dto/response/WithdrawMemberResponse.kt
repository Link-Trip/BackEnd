package com.linktrip.input.http.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 탈퇴 응답")
data class WithdrawMemberResponse(
    @field:Schema(description = "삭제된 여행 계획 수", example = "3")
    val deletedTripPlanCount: Int,
)
