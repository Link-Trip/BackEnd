package com.linktrip.application.domain.trip

import com.linktrip.application.domain.video.Category
import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.port.input.UpdateTripPlanCommand
import com.linktrip.application.port.input.UpdateTripPlanItemCommand
import com.linktrip.application.port.output.persistence.HashtagPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanItemWithItinerary
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanSummaryRow
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class TripPlanServiceTest {
    @Mock
    lateinit var planPort: TripPlanPersistencePort

    @Mock
    lateinit var planItemPort: TripPlanItemPersistencePort

    @Mock
    lateinit var itineraryItemPort: TravelItineraryItemPersistencePort

    @Mock
    lateinit var requestPort: TripPlanRequestPersistencePort

    @Mock
    lateinit var hashtagPort: HashtagPersistencePort

    private lateinit var service: TripPlanService

    @BeforeEach
    fun setUp() {
        // @InjectMocks 는 Long 같은 단순 타입을 주입 못 하므로 명시 생성.
        service =
            TripPlanService(
                planPort = planPort,
                planItemPort = planItemPort,
                itineraryItemPort = itineraryItemPort,
                requestPort = requestPort,
                hashtagPort = hashtagPort,
                dailyVideoAnalyzeLimit = 10L,
            )
    }

    @Nested
    inner class RegisterRequest {
        @Test
        fun `이미 등록된 요청이면_중복 등록하지 않는다`() {
            whenever(requestPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(true)

            service.registerRequest("m1", "t1")

            verify(requestPort, never()).save(any())
        }

        @Test
        fun `등록되지 않은 요청이면_대기열에 저장한다`() {
            whenever(requestPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(false)

            service.registerRequest("m1", "t1")

            val captor = argumentCaptor<TripPlanRequest>()
            verify(requestPort).save(captor.capture())
            assertEquals("m1", captor.firstValue.memberId)
            assertEquals("t1", captor.firstValue.videoAnalysisTaskId)
        }
    }

    @Nested
    inner class CreateFromAnalysisIfAbsent {
        @Test
        fun `memberId가 빈 문자열이면_어떤 조회도 하지 않고 즉시 종료한다`() {
            service.createFromAnalysisIfAbsent("", "t1")

            verify(planPort, never()).existsByMemberIdAndVideoAnalysisTaskId(any(), any())
            verify(planPort, never()).save(any())
        }

        @Test
        fun `이미 TripPlan이 존재하면_일정 항목을 조회하지 않고 종료한다`() {
            whenever(planPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(true)

            service.createFromAnalysisIfAbsent("m1", "t1")

            verify(itineraryItemPort, never()).findByVideoAnalysisTaskId(any())
            verify(planPort, never()).save(any())
        }

        @Test
        fun `일정 항목이 없으면_TripPlan을 생성하지 않는다`() {
            whenever(planPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(false)
            whenever(itineraryItemPort.findByVideoAnalysisTaskId("t1")).thenReturn(emptyList())

            service.createFromAnalysisIfAbsent("m1", "t1")

            verify(planPort, never()).save(any())
        }

        @Test
        fun `일정 항목 3개가 있으면_TripPlan 1개와 TripPlanItem 3개가 생성된다`() {
            whenever(planPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(false)
            whenever(itineraryItemPort.findByVideoAnalysisTaskId("t1")).thenReturn(
                listOf(
                    createItineraryItem("i1", 1, 1),
                    createItineraryItem("i2", 1, 2),
                    createItineraryItem("i3", 2, 1),
                ),
            )
            whenever(planPort.save(any())).thenAnswer { it.arguments[0] as TripPlan }

            service.createFromAnalysisIfAbsent("m1", "t1", "도쿄 여행")

            val planCaptor = argumentCaptor<TripPlan>()
            verify(planPort).save(planCaptor.capture())
            assertEquals("m1", planCaptor.firstValue.memberId)
            assertEquals("t1", planCaptor.firstValue.videoAnalysisTaskId)
            assertEquals("도쿄 여행", planCaptor.firstValue.title)

            val itemsCaptor = argumentCaptor<List<TripPlanItem>>()
            verify(planItemPort).saveAll(itemsCaptor.capture())
            assertEquals(3, itemsCaptor.firstValue.size)
            assertEquals("i1", itemsCaptor.firstValue[0].travelItineraryItemId)
            assertEquals("i2", itemsCaptor.firstValue[1].travelItineraryItemId)
            assertEquals("i3", itemsCaptor.firstValue[2].travelItineraryItemId)
        }

        @Test
        fun `생성된 TripPlanItem의 day와 itemOrder가 원본 일정 항목과 일치한다`() {
            whenever(planPort.existsByMemberIdAndVideoAnalysisTaskId("m1", "t1")).thenReturn(false)
            whenever(itineraryItemPort.findByVideoAnalysisTaskId("t1")).thenReturn(
                listOf(
                    createItineraryItem("i1", 2, 3),
                ),
            )
            whenever(planPort.save(any())).thenAnswer { it.arguments[0] as TripPlan }

            service.createFromAnalysisIfAbsent("m1", "t1")

            val itemsCaptor = argumentCaptor<List<TripPlanItem>>()
            verify(planItemPort).saveAll(itemsCaptor.capture())
            val item = itemsCaptor.firstValue[0]
            assertEquals(2, item.day)
            assertEquals(3, item.itemOrder)
        }
    }

    @Nested
    inner class GetTripPlans {
        @Test
        fun `결과가 size보다 많으면_hasNext가 true이고 nextCursor가 마지막 항목의 createdAt이다`() {
            val rows = (1..3).map { createSummaryRow("p$it") }
            whenever(planPort.findSummariesByMemberId("m1", null, 3)).thenReturn(rows)

            val result = service.getTripPlans("m1", null, 2)

            assertTrue(result.hasNext)
            assertEquals(2, result.items.size)
            assertEquals(result.items.last().tripPlan.createdAt.toString(), result.nextCursor)
        }

        @Test
        fun `결과가 정확히 size개이면_hasNext가 false이다`() {
            val rows = listOf(createSummaryRow("p1"), createSummaryRow("p2"))
            whenever(planPort.findSummariesByMemberId("m1", null, 3)).thenReturn(rows)

            val result = service.getTripPlans("m1", null, 2)

            assertFalse(result.hasNext)
            assertEquals(2, result.items.size)
            assertNull(result.nextCursor)
        }

        @Test
        fun `결과가 0개이면_빈 목록과 hasNext false를 반환한다`() {
            whenever(planPort.findSummariesByMemberId("m1", null, 21)).thenReturn(emptyList())

            val result = service.getTripPlans("m1", null, 20)

            assertFalse(result.hasNext)
            assertTrue(result.items.isEmpty())
            assertNull(result.nextCursor)
        }
    }

    @Nested
    inner class GetTripPlanDetail {
        @Test
        fun `존재하지 않는 ID로 조회하면_NOT_FOUND_TRIP_PLAN 예외가 발생한다`() {
            whenever(planPort.findById("not-exist")).thenReturn(null)

            val exception =
                assertThrows<LinktripException> {
                    service.getTripPlanDetail("m1", "not-exist")
                }

            assertEquals(ExceptionCode.NOT_FOUND_TRIP_PLAN.statusCode, exception.statusCode)
        }

        @Test
        fun `다른 사용자의 TripPlan을 조회하면_FORBIDDEN_TRIP_PLAN 예외가 발생한다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1", memberId = "other"))

            val exception =
                assertThrows<LinktripException> {
                    service.getTripPlanDetail("m1", "p1")
                }

            assertEquals(ExceptionCode.FORBIDDEN_TRIP_PLAN.statusCode, exception.statusCode)
        }

        @Test
        fun `자신의 TripPlan을 조회하면_plan 정보와 활성 항목 목록을 반환한다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))
            whenever(planItemPort.findActiveWithItineraryAndPlaceByTripPlanId("p1")).thenReturn(
                listOf(
                    TripPlanItemWithItinerary(
                        tripPlanItem = createTripPlanItem("pi1", "p1", "i1"),
                        travelItineraryItem = createItineraryItem("i1", 1, 1),
                    ),
                    TripPlanItemWithItinerary(
                        tripPlanItem = createTripPlanItem("pi2", "p1", "i2"),
                        travelItineraryItem = createItineraryItem("i2", 1, 2),
                    ),
                ),
            )

            val result = service.getTripPlanDetail("m1", "p1")

            assertEquals("p1", result.tripPlan.id)
            assertEquals(2, result.items.size)
            assertEquals("i1", result.items[0].travelItineraryItem.id)
            assertEquals("i2", result.items[1].travelItineraryItem.id)
        }
    }

    @Nested
    inner class UpdateTripPlan {
        @Test
        fun `제목만 수정하면_updateTitle만 호출되고 updateItems는 호출되지 않는다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))
            whenever(planItemPort.findActiveWithItineraryAndPlaceByTripPlanId("p1")).thenReturn(emptyList())

            service.updateTripPlan("m1", "p1", UpdateTripPlanCommand(title = "새 제목", items = null))

            verify(planPort).updateTitle("p1", "새 제목")
            verify(planItemPort, never()).updateItems(any(), any())
        }

        @Test
        fun `항목만 수정하면_updateItems만 호출되고 updateTitle은 호출되지 않는다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))
            whenever(planItemPort.findActiveWithItineraryAndPlaceByTripPlanId("p1")).thenReturn(emptyList())

            val items = listOf(UpdateTripPlanItemCommand("pi1", 2, 1))
            service.updateTripPlan("m1", "p1", UpdateTripPlanCommand(title = null, items = items))

            verify(planPort, never()).updateTitle(any(), any())
            verify(planItemPort).updateItems("p1", items)
        }

        @Test
        fun `제목과 항목을 동시에 수정하면_둘 다 호출된다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))
            whenever(planItemPort.findActiveWithItineraryAndPlaceByTripPlanId("p1")).thenReturn(emptyList())

            val items = listOf(UpdateTripPlanItemCommand("pi1", 1, 1))
            service.updateTripPlan("m1", "p1", UpdateTripPlanCommand(title = "변경", items = items))

            verify(planPort).updateTitle("p1", "변경")
            verify(planItemPort).updateItems("p1", items)
        }

        @Test
        fun `다른 사용자의 TripPlan을 수정하면_FORBIDDEN 예외가 발생한다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1", memberId = "other"))

            val exception =
                assertThrows<LinktripException> {
                    service.updateTripPlan("m1", "p1", UpdateTripPlanCommand(title = "해킹", items = null))
                }

            assertEquals(ExceptionCode.FORBIDDEN_TRIP_PLAN.statusCode, exception.statusCode)
        }
    }

    @Nested
    inner class DeleteTripPlan {
        @Test
        fun `자신의 TripPlan을 삭제하면_item과 plan 모두 삭제된다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))

            service.deleteTripPlan("m1", "p1")

            verify(planItemPort).deleteByTripPlanId("p1")
            verify(planPort).deleteById("p1")
        }

        @Test
        fun `다른 사용자의 TripPlan을 삭제하면_FORBIDDEN 예외가 발생하고 삭제되지 않는다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1", memberId = "other"))

            assertThrows<LinktripException> {
                service.deleteTripPlan("m1", "p1")
            }

            verify(planItemPort, never()).deleteByTripPlanId(any())
            verify(planPort, never()).deleteById(any())
        }

        @Test
        fun `존재하지 않는 TripPlan을 삭제하면_NOT_FOUND 예외가 발생한다`() {
            whenever(planPort.findById("not-exist")).thenReturn(null)

            val exception =
                assertThrows<LinktripException> {
                    service.deleteTripPlan("m1", "not-exist")
                }

            assertEquals(ExceptionCode.NOT_FOUND_TRIP_PLAN.statusCode, exception.statusCode)
        }

        @Test
        fun `삭제 시_item이 plan보다 먼저 삭제된다`() {
            whenever(planPort.findById("p1")).thenReturn(createTripPlan("p1"))

            service.deleteTripPlan("m1", "p1")

            val inOrder = org.mockito.kotlin.inOrder(planItemPort, planPort)
            inOrder.verify(planItemPort).deleteByTripPlanId("p1")
            inOrder.verify(planPort).deleteById("p1")
        }
    }

    // helpers

    private fun createTripPlan(
        id: String,
        memberId: String = "m1",
    ): TripPlan =
        TripPlan(
            id = id,
            memberId = memberId,
            videoAnalysisTaskId = "t1",
            title = "여행 계획",
        )

    private fun createTripPlanItem(
        id: String,
        tripPlanId: String,
        itineraryItemId: String,
    ): TripPlanItem =
        TripPlanItem(
            id = id,
            tripPlanId = tripPlanId,
            travelItineraryItemId = itineraryItemId,
            day = 1,
            itemOrder = 1,
        )

    private fun createItineraryItem(
        id: String,
        day: Int,
        order: Int,
    ): TravelItineraryItem =
        TravelItineraryItem(
            id = id,
            videoAnalysisTaskId = "t1",
            day = day,
            itemOrder = order,
            category = Category.ATTRACTION,
            name = "장소$id",
            description = null,
            tips = null,
        )

    private fun createSummaryRow(id: String): TripPlanSummaryRow =
        TripPlanSummaryRow(
            tripPlan = createTripPlan(id),
            youtubeUrl = "https://youtube.com/$id",
            activeItemCount = 1,
            days = 3,
        )
}
