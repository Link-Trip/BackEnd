package com.linktrip.application.domain.trip

import com.linktrip.application.port.output.persistence.DestinationTripPlanCount
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DiscoverCountryServiceTest {
    @Mock
    lateinit var tripPlanPersistencePort: TripPlanPersistencePort

    @InjectMocks
    lateinit var service: DiscoverCountryService

    @Test
    fun `destination에서 국가를 파싱하여_같은 국가의 일정 수를 합산한다`() {
        // given - 같은 국가(일본)의 서로 다른 도시 destination
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(
            listOf(
                DestinationTripPlanCount("도쿄, 일본", 3),
                DestinationTripPlanCount("오사카, 일본", 2),
                DestinationTripPlanCount("방콕, 태국", 4),
            ),
        )

        // when - 상위 국가를 조회한다
        val result = service.getTopCountries()

        // then - 일본 5건(3+2), 태국 4건으로 합산·정렬된다
        assertEquals(2, result.size)
        assertEquals("일본", result[0].country)
        assertEquals(5L, result[0].tripPlanCount)
        assertEquals("태국", result[1].country)
        assertEquals(4L, result[1].tripPlanCount)
    }

    @Test
    fun `콤마가 없는 destination은_전체를 국가로 취급한다`() {
        // given - 국가 단독 destination
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(
            listOf(
                DestinationTripPlanCount("일본", 2),
                DestinationTripPlanCount("도쿄, 일본", 1),
            ),
        )

        // when
        val result = service.getTopCountries()

        // then - "일본"으로 합산된다
        assertEquals(1, result.size)
        assertEquals("일본", result[0].country)
        assertEquals(3L, result[0].tripPlanCount)
    }

    @Test
    fun `국가가 10개를 넘으면_일정 수 상위 10개만 반환한다`() {
        // given - 12개 국가, 일정 수 1~12
        val rows = (1..12).map { DestinationTripPlanCount("도시, 국가$it", it.toLong()) }
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(rows)

        // when
        val result = service.getTopCountries()

        // then - 상위 10개만, 내림차순으로 반환된다
        assertEquals(10, result.size)
        assertEquals(12L, result[0].tripPlanCount)
        assertEquals(3L, result[9].tripPlanCount)
        assertTrue(result.none { it.tripPlanCount < 3L })
    }

    @Test
    fun `일정 수가 같으면_국가명 오름차순으로 정렬된다`() {
        // given - 동일한 일정 수의 두 국가
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(
            listOf(
                DestinationTripPlanCount("방콕, 태국", 2),
                DestinationTripPlanCount("도쿄, 일본", 2),
            ),
        )

        // when
        val result = service.getTopCountries()

        // then - 가나다순(일본 → 태국)
        assertEquals("일본", result[0].country)
        assertEquals("태국", result[1].country)
    }

    @Test
    fun `국가를 파싱할 수 없는 destination은_결과에서 제외된다`() {
        // given - 콤마 뒤가 공백인 비정상 destination
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(
            listOf(
                DestinationTripPlanCount("도쿄, ", 3),
                DestinationTripPlanCount("방콕, 태국", 1),
            ),
        )

        // when
        val result = service.getTopCountries()

        // then - 파싱 불가 행은 제외된다
        assertEquals(1, result.size)
        assertEquals("태국", result[0].country)
    }

    @Test
    fun `생성된 일정이 없으면_빈 목록을 반환한다`() {
        // given - 일정 없음
        whenever(tripPlanPersistencePort.countGroupedByDestination()).thenReturn(emptyList())

        // when
        val result = service.getTopCountries()

        // then
        assertTrue(result.isEmpty())
    }
}
