package com.linktrip.input.http.controller.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class UpdateTripPlanRequest(
    val title: String?,
    @field:Valid
    val items: List<UpdateTripPlanItemRequest>?,
)

data class UpdateTripPlanItemRequest(
    @field:NotBlank
    val tripPlanItemId: String,
    @field:Positive
    val day: Int,
    @field:Positive
    val itemOrder: Int,
)
