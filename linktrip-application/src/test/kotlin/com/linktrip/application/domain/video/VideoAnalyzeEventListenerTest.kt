package com.linktrip.application.domain.video

import com.linktrip.application.domain.trip.TripPlanService
import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class VideoAnalyzeEventListenerTest {
    @Mock
    lateinit var videoAnalyzePort: VideoAnalyzePort

    @Mock
    lateinit var videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort

    @Mock
    lateinit var videoAnalysisResultSaver: VideoAnalysisResultSaver

    @Mock
    lateinit var placeEnrichService: PlaceEnrichService

    @Mock
    lateinit var videoAnalysisNotificationPort: VideoAnalysisNotificationPort

    @Mock
    lateinit var tripPlanRequestPort: TripPlanRequestPersistencePort

    @Mock
    lateinit var tripPlanService: TripPlanService

    @InjectMocks
    lateinit var listener: VideoAnalyzeEventListener

    @Test
    fun `유효한 여행 영상이면_AI분석_결과저장_장소보강_완료알림 순서로 전체 파이프라인이 실행된다`() {
        // given - 유효한 여행 영상의 분석 이벤트
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult =
            VideoAnalysisResult(
                valid = true,
                destination = "도쿄",
                title = "도쿄 3박 4일 여행",
                estimatedMinCost = 800000,
                estimatedMaxCost = 1200000,
                costBasis = CostBasis.VIDEO_MENTIONED,
                hashtags = listOf("맛집여행", "문화탐방"),
                days =
                    listOf(
                        VideoAnalysisResult.DaySchedule(
                            day = 1,
                            items =
                                listOf(
                                    VideoAnalysisResult.ScheduleItem(1, Category.EAT, "스시집", "맛있는 스시", null),
                                ),
                        ),
                    ),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - AI분석 -> 결과저장 -> 장소보강 -> 완료알림 순서로 실행된다
        val inOrder =
            inOrder(videoAnalyzePort, videoAnalysisResultSaver, placeEnrichService, videoAnalysisNotificationPort)
        inOrder.verify(videoAnalyzePort).analyze("https://youtube.com/1")
        inOrder.verify(videoAnalysisResultSaver).save(
            eq("s1"),
            any(),
            eq(800000L),
            eq(1200000L),
            eq(CostBasis.VIDEO_MENTIONED),
            eq(listOf("맛집여행", "문화탐방")),
        )
        inOrder.verify(placeEnrichService).enrichPlaces("s1", "도쿄")
        inOrder.verify(videoAnalysisNotificationPort).notifyAnalysisComplete(any(), any())
    }

    @Test
    fun `여행 영상이 아닌 것으로 판정되면_INVALID 상태로 변경하고_일정 저장과 장소 보강을 수행하지 않는다`() {
        // given - 여행 영상이 아닌 것으로 판정된 분석 결과
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val invalidResult =
            VideoAnalysisResult(
                valid = false,
                destination = null,
                title = null,
                estimatedMinCost = null,
                estimatedMaxCost = null,
                costBasis = null,
                hashtags = emptyList(),
                days = emptyList(),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(invalidResult)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - INVALID 상태로 변경하고, 저장과 보강은 수행하지 않는다
        verify(
            videoAnalysisTaskPersistencePort,
        ).updateValidAndStatus(
            eq("s1"),
            eq(false),
            eq(VideoAnalysisTaskStatus.INVALID),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
        verify(videoAnalysisResultSaver, never()).save(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), any())
        verify(placeEnrichService, never()).enrichPlaces(any(), anyOrNull())
        verify(videoAnalysisNotificationPort, never()).notifyAnalysisComplete(any(), any())
    }

    @Test
    fun `AI 분석 중 예외가 발생하면_FAILED 상태로 변경하고_이후 단계를 모두 건너뛴다`() {
        // given - AI 분석 시 예외가 발생하는 상황
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenThrow(RuntimeException("AI 오류"))

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - FAILED 상태로 변경하고, 결과 저장은 수행하지 않는다
        verify(videoAnalysisTaskPersistencePort).updateStatus("s1", VideoAnalysisTaskStatus.FAILED)
        verify(videoAnalysisResultSaver, never()).save(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), any())
    }

    @Test
    fun `2일치 3개 일정 항목이 있는 분석 결과를_TravelItineraryItem으로 변환하면_day와 category와 순서가 정확히 매핑된다`() {
        // given - 2일치 3개 일정 항목이 포함된 분석 결과
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult =
            VideoAnalysisResult(
                valid = true,
                destination = "파리",
                title = "파리 2일 여행",
                estimatedMinCost = 500000,
                estimatedMaxCost = 700000,
                costBasis = CostBasis.ITEM_ESTIMATED,
                hashtags = listOf("문화탐방", "쇼핑"),
                days =
                    listOf(
                        VideoAnalysisResult.DaySchedule(
                            day = 1,
                            items =
                                listOf(
                                    VideoAnalysisResult.ScheduleItem(1, Category.ATTRACTION, "에펠탑", null, null),
                                    VideoAnalysisResult.ScheduleItem(2, Category.EAT, "카페", null, null),
                                ),
                        ),
                        VideoAnalysisResult.DaySchedule(
                            day = 2,
                            items =
                                listOf(
                                    VideoAnalysisResult.ScheduleItem(1, Category.SHOPPING, "샹젤리제", null, null),
                                ),
                        ),
                    ),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - 3개 항목이 day와 category와 순서가 정확히 매핑되어 저장된다
        val captor = org.mockito.kotlin.argumentCaptor<List<TravelItineraryItem>>()
        verify(videoAnalysisResultSaver).save(
            eq("s1"),
            captor.capture(),
            eq(500000L),
            eq(700000L),
            eq(CostBasis.ITEM_ESTIMATED),
            eq(listOf("문화탐방", "쇼핑")),
        )

        val savedItems = captor.firstValue
        assertEquals(3, savedItems.size)
        assertEquals(1, savedItems[0].day)
        assertEquals("에펠탑", savedItems[0].name)
        assertEquals(Category.ATTRACTION, savedItems[0].category)
        assertEquals(1, savedItems[1].day)
        assertEquals("카페", savedItems[1].name)
        assertEquals(Category.EAT, savedItems[1].category)
        assertEquals(2, savedItems[2].day)
        assertEquals("샹젤리제", savedItems[2].name)
        assertEquals(Category.SHOPPING, savedItems[2].category)
    }
}
