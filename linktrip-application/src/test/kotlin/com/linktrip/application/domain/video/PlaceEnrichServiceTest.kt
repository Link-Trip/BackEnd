package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.GooglePlacesPort
import com.linktrip.application.port.output.persistence.PlaceEnrichPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PlaceEnrichServiceTest {
    @Mock
    lateinit var googlePlacesPort: GooglePlacesPort

    @Mock
    lateinit var placeEnrichPersistencePort: PlaceEnrichPersistencePort

    @Mock
    lateinit var travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort

    private fun createService() =
        PlaceEnrichService(
            googlePlacesPort = googlePlacesPort,
            placeEnrichPersistencePort = placeEnrichPersistencePort,
            travelItineraryItemPersistencePort = travelItineraryItemPersistencePort,
            placeEnrichDispatcher = Dispatchers.Unconfined,
        )

    @Test
    fun `재검색 가능한 일정 항목이 없으면_Google Places API를 호출하지 않는다`() {
        // given - 재검색 대상 항목이 없는 상태
        whenever(travelItineraryItemPersistencePort.findRetryableItems("s1")).thenReturn(emptyList())

        // when - 장소 보강을 실행한다
        createService().enrichPlaces("s1", "도쿄")

        // then - API를 호출하지 않고, 결과도 저장하지 않는다
        verify(googlePlacesPort, never()).searchPlace(any(), any())
        verify(placeEnrichPersistencePort, never()).applyResults(any(), any())
    }

    @Test
    fun `목적지 정보가 있으면_장소 검색 시 destination을 함께 전달하여 정확도를 높인다`() {
        // given - 보강 대상 항목 2개와 destination "도쿄"
        val items =
            listOf(
                createItem("item-1", "도쿄 타워"),
                createItem("item-2", "시부야 스크램블"),
            )
        whenever(travelItineraryItemPersistencePort.findRetryableItems("s1")).thenReturn(items)

        val placeResult = PlaceSearchResult("gp1", "도쿄 타워", "address", 35.0, 139.0)
        whenever(googlePlacesPort.searchPlace("도쿄 타워", "도쿄")).thenReturn(placeResult)
        whenever(googlePlacesPort.searchPlace("시부야 스크램블", "도쿄")).thenReturn(null)

        // when - destination "도쿄"와 함께 장소 보강을 실행한다
        createService().enrichPlaces("s1", "도쿄")

        // then - 결과가 저장되고, 찾지 못한 장소도 success=true, place=null로 처리된다
        val captor = argumentCaptor<List<PlaceEnrichResult>>()
        verify(placeEnrichPersistencePort).applyResults(eq("s1"), captor.capture())

        val results = captor.firstValue
        assertEquals(2, results.size)
        assertEquals(true, results[0].success)
        assertEquals(placeResult, results[0].place)
        assertEquals(true, results[1].success)
        assertEquals(null, results[1].place)
    }

    @Test
    fun `3개 장소 중 1개에서 API 예외가 발생해도_나머지 2개는 정상 처리되고_실패한 항목만 success=false가 된다`() {
        // given - 보강 대상 항목 3개, 2번째 항목에서 API 예외 발생
        val items =
            listOf(
                createItem("item-1", "장소1"),
                createItem("item-2", "장소2"),
                createItem("item-3", "장소3"),
            )
        whenever(travelItineraryItemPersistencePort.findRetryableItems("s1")).thenReturn(items)

        whenever(googlePlacesPort.searchPlace("장소1", null))
            .thenReturn(PlaceSearchResult("gp1", "장소1", null, null, null))
        whenever(googlePlacesPort.searchPlace("장소2", null))
            .thenThrow(RuntimeException("API 오류"))
        whenever(googlePlacesPort.searchPlace("장소3", null))
            .thenReturn(PlaceSearchResult("gp3", "장소3", null, null, null))

        // when - destination 없이 장소 보강을 실행한다
        createService().enrichPlaces("s1")

        // then - 실패한 항목만 success=false이고, 나머지는 정상 처리된다
        val captor = argumentCaptor<List<PlaceEnrichResult>>()
        verify(placeEnrichPersistencePort).applyResults(eq("s1"), captor.capture())

        val results = captor.firstValue
        assertEquals(3, results.size)
        assertEquals(true, results[0].success)
        assertEquals(false, results[1].success)
        assertEquals(true, results[2].success)
    }

    @Test
    fun `retryAll 호출 시 재검색 대상이 없으면_아무 처리도 하지 않는다`() {
        // given - 재검색 대상 videoAnalysisTaskId가 없는 상태
        whenever(travelItineraryItemPersistencePort.findVideoAnalysisTaskIdsWithRetryableItems())
            .thenReturn(emptyList())

        // when - retryAll을 호출한다
        createService().retryAll()

        // then - 개별 항목 조회를 수행하지 않는다
        verify(travelItineraryItemPersistencePort, never()).findRetryableItems(any())
    }

    @Test
    fun `retryAll 중 하나의 영상에서 DB 오류가 발생해도_나머지 영상들은 계속 처리한다`() {
        // given - 재검색 대상 videoAnalysisTaskId 2개, s1에서 DB 오류 발생
        whenever(travelItineraryItemPersistencePort.findVideoAnalysisTaskIdsWithRetryableItems())
            .thenReturn(listOf("s1", "s2"))
        whenever(travelItineraryItemPersistencePort.findRetryableItems("s1"))
            .thenThrow(RuntimeException("DB 연결 오류"))
        whenever(travelItineraryItemPersistencePort.findRetryableItems("s2")).thenReturn(emptyList())

        // when - retryAll을 호출한다
        createService().retryAll()

        // then - s1은 실패하지만 s2는 정상적으로 처리된다
        verify(travelItineraryItemPersistencePort).findRetryableItems("s1")
        verify(travelItineraryItemPersistencePort).findRetryableItems("s2")
    }

    private fun createItem(
        id: String,
        name: String,
    ) = TravelItineraryItem(
        id = id,
        videoAnalysisTaskId = "s1",
        day = 1,
        itemOrder = 1,
        category = Category.ATTRACTION,
        name = name,
        description = null,
        tips = null,
    )
}
