package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.port.input.DiscoverCountryUseCase.CountryTripPlanCount
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "대표 여행지(나라) 정보")
data class DiscoverCountryResponse(
    @field:Schema(description = "나라 이름", example = "일본")
    val country: String,
    @field:Schema(description = "해당 나라로 생성된 일정 수", example = "42")
    val tripPlanCount: Long,
) {
    companion object {
        fun from(count: CountryTripPlanCount): DiscoverCountryResponse =
            DiscoverCountryResponse(
                country = count.country,
                tripPlanCount = count.tripPlanCount,
            )
    }
}

@Schema(description = "대표 여행지(나라) 목록 응답")
data class DiscoverCountryResponses(
    @field:Schema(description = "일정 생성 수 기준 상위 나라 목록 (최대 10개, 내림차순)")
    val countries: List<DiscoverCountryResponse>,
) {
    companion object {
        fun from(counts: List<CountryTripPlanCount>): DiscoverCountryResponses =
            DiscoverCountryResponses(
                countries = counts.map { DiscoverCountryResponse.from(it) },
            )
    }
}
