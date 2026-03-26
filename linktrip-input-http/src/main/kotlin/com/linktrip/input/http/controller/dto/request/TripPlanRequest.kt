package com.linktrip.input.http.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class UpdateTripPlanRequest(
    val title: String?,
    val items: List<UpdateTripPlanItemRequest>?,
)

data class UpdateTripPlanItemRequest(
    @field:NotBlank
    val tripPlanItemId: String,
    val day: Int,
    val itemOrder: Int,
)
