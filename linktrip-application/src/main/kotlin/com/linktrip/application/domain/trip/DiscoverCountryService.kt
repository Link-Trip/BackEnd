package com.linktrip.application.domain.trip

import com.linktrip.application.port.input.DiscoverCountryUseCase
import com.linktrip.application.port.input.DiscoverCountryUseCase.CountryTripPlanCount
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자들이 생성한 일정(trip_plan)이 많은 나라 순위를 제공한다.
 *
 * destination 은 "도시, 국가" 형식(예: "도쿄, 일본")이므로 마지막 콤마 뒤를 국가로 파싱하고,
 * 콤마가 없으면 destination 전체를 국가로 취급한다 (예: "일본").
 */
@Service
class DiscoverCountryService(
    private val tripPlanPersistencePort: TripPlanPersistencePort,
) : DiscoverCountryUseCase {
    @Transactional(readOnly = true)
    override fun getTopCountries(): List<CountryTripPlanCount> =
        tripPlanPersistencePort.countGroupedByDestination()
            .mapNotNull { row ->
                extractCountry(row.destination)?.let { country -> country to row.count }
            }
            .groupBy({ it.first }, { it.second })
            .map { (country, counts) -> CountryTripPlanCount(country, counts.sum()) }
            .sortedWith(compareByDescending<CountryTripPlanCount> { it.tripPlanCount }.thenBy { it.country })
            .take(TOP_COUNTRY_SIZE)

    private fun extractCountry(destination: String): String? =
        destination.substringAfterLast(',').trim().ifBlank { null }

    companion object {
        private const val TOP_COUNTRY_SIZE = 10
    }
}
