package com.linktrip.input.http.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

@Schema(description = "여행 계획 수정 요청")
data class UpdateTripPlanRequest(
    @field:Schema(description = "변경할 제목 (null이면 기존 제목 유지)", example = "도쿄 맛집 투어", nullable = true)
    val title: String?,
    @field:Valid
    @field:Schema(description = "변경할 일정 아이템 목록 (null이면 기존 순서 유지)", nullable = true)
    val items: List<UpdateTripPlanItemRequest>?,
)

@Schema(description = "여행 계획 아이템 순서 변경 요청")
data class UpdateTripPlanItemRequest(
    @field:NotBlank
    @field:Schema(description = "여행 계획 아이템 ID", example = "019d4205-1234-7a5b-8c6d-789012345678")
    val tripPlanItemId: String,
    @field:Positive
    @field:Schema(description = "변경할 일차 (1 이상)", example = "2")
    val day: Int,
    @field:Positive
    @field:Schema(description = "변경할 순서 (1 이상)", example = "1")
    val itemOrder: Int,
)
