package com.linktrip.application.port.input

interface DiscoverCountryUseCase {
    fun getTopCountries(): List<CountryTripPlanCount>

    data class CountryTripPlanCount(
        val country: String,
        val tripPlanCount: Long,
    )
}
