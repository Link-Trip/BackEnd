package com.linktrip.input.http.controller

import com.linktrip.application.port.input.TripPlanUseCase
import com.linktrip.application.port.input.UpdateTripPlanCommand
import com.linktrip.application.port.input.UpdateTripPlanItemCommand
import com.linktrip.input.http.auth.AuthenticatedMember
import com.linktrip.input.http.config.PaginationDefaults
import com.linktrip.input.http.controller.dto.request.UpdateTripPlanRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.TripPlanCursorResponse
import com.linktrip.input.http.controller.dto.response.TripPlanDetailResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/trip-plans")
class TripPlanController(
    private val tripPlanUseCase: TripPlanUseCase,
) {
    @GetMapping
    fun getTripPlans(
        @AuthenticatedMember memberId: String,
        @RequestParam(required = false) cursor: String?,
    ): ApiResponse<TripPlanCursorResponse> {
        val cursorDateTime = cursor?.let { java.time.LocalDateTime.parse(it) }
        val result = tripPlanUseCase.getTripPlans(memberId, cursorDateTime, PaginationDefaults.TRIP_PLAN_SIZE)
        return ApiResponse.ok(TripPlanCursorResponse.from(result))
    }

    @GetMapping("/{tripPlanId}")
    fun getTripPlanDetail(
        @AuthenticatedMember memberId: String,
        @PathVariable tripPlanId: String,
    ): ApiResponse<TripPlanDetailResponse> {
        val detail = tripPlanUseCase.getTripPlanDetail(memberId, tripPlanId)
        return ApiResponse.ok(TripPlanDetailResponse.from(detail))
    }

    @PutMapping("/{tripPlanId}")
    fun updateTripPlan(
        @AuthenticatedMember memberId: String,
        @PathVariable tripPlanId: String,
        @Validated @RequestBody request: UpdateTripPlanRequest,
    ): ApiResponse<TripPlanDetailResponse> {
        val command =
            UpdateTripPlanCommand(
                title = request.title,
                items =
                    request.items?.map {
                        UpdateTripPlanItemCommand(
                            tripPlanItemId = it.tripPlanItemId,
                            day = it.day,
                            itemOrder = it.itemOrder,
                        )
                    },
            )
        val detail = tripPlanUseCase.updateTripPlan(memberId, tripPlanId, command)
        return ApiResponse.ok(TripPlanDetailResponse.from(detail))
    }

    @DeleteMapping("/{tripPlanId}")
    fun deleteTripPlan(
        @AuthenticatedMember memberId: String,
        @PathVariable tripPlanId: String,
    ): ApiResponse<Unit> {
        tripPlanUseCase.deleteTripPlan(memberId, tripPlanId)
        return ApiResponse.ok()
    }
}
