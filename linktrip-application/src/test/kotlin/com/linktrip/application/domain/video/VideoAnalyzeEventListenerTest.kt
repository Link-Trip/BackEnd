package com.linktrip.application.domain.video

import com.linktrip.application.domain.trip.TripPlanRequest
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
import org.mockito.kotlin.argumentCaptor
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
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when
        listener.handle(event)

        // then - AI분석 → 결과저장 → 장소보강 → 완료알림 순서로 실행된다
        val inOrder =
            inOrder(videoAnalyzePort, videoAnalysisResultSaver, placeEnrichService, videoAnalysisNotificationPort)
        inOrder.verify(videoAnalyzePort).analyze("https://youtube.com/1")
        inOrder.verify(
            videoAnalysisResultSaver,
        ).save(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull())
        inOrder.verify(placeEnrichService).enrichPlaces("s1", "도쿄")
        inOrder.verify(videoAnalysisNotificationPort).notifyAnalysisComplete(any(), any())
    }

    @Test
    fun `여행 영상이 아닌 것으로 판정되면_INVALID 상태로 변경하고_일정 저장과 장소 보강을 수행하지 않는다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val invalidResult =
            VideoAnalysisResult(
                valid = false,
                destination = null,
                title = null,
                summary = null,
                estimatedMinCost = null,
                estimatedMaxCost = null,
                costBasis = null,
                hashtags = emptyList(),
                days = emptyList(),
                timeline = emptyList(),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(invalidResult)

        // when
        listener.handle(event)

        // then - INVALID 상태로 변경하고, 저장과 보강은 수행하지 않는다
        verify(videoAnalysisTaskPersistencePort).updateValidAndStatus(
            eq("s1"),
            eq(false),
            eq(VideoAnalysisTaskStatus.INVALID),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
        verify(
            videoAnalysisResultSaver,
            never(),
        ).save(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull())
        verify(placeEnrichService, never()).enrichPlaces(any(), anyOrNull())
        verify(videoAnalysisNotificationPort, never()).notifyAnalysisComplete(any(), any())
    }

    @Test
    fun `AI 분석 중 예외가 발생하면_FAILED 상태로 변경하고_이후 단계를 모두 건너뛴다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenThrow(RuntimeException("AI 오류"))

        // when
        listener.handle(event)

        // then
        verify(videoAnalysisTaskPersistencePort).updateStatus("s1", VideoAnalysisTaskStatus.FAILED)
        verify(
            videoAnalysisResultSaver,
            never(),
        ).save(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `2일치 3개 일정 항목이 있는 분석 결과를_TravelItineraryItem으로 변환하면_day와 category와 순서가 정확히 매핑된다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult =
            VideoAnalysisResult(
                valid = true,
                destination = "파리",
                title = "파리 2일 여행",
                summary = null,
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
                timeline = emptyList(),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when
        listener.handle(event)

        // then - 3개 항목이 day와 category와 순서가 정확히 매핑되어 저장된다
        val itemsCaptor = argumentCaptor<List<TravelItineraryItem>>()
        verify(videoAnalysisResultSaver).save(
            eq("s1"),
            itemsCaptor.capture(),
            anyOrNull(),
            eq(500000L),
            eq(700000L),
            eq(CostBasis.ITEM_ESTIMATED),
            eq(listOf("문화탐방", "쇼핑")),
            any(),
            anyOrNull(),
        )

        val savedItems = itemsCaptor.firstValue
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

    @Test
    fun `summary가 있는 분석 결과이면_summary가 save에 올바르게 전달된다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val summary = "도쿄의 숨겨진 맛집을 탐방하는 여행 영상입니다."
        val analysisResult = validAnalysisResult(summary = summary)
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when
        listener.handle(event)

        // then - summary가 그대로 전달된다
        verify(videoAnalysisResultSaver).save(
            eq("s1"),
            any(),
            eq(summary),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            any(),
            any(),
            anyOrNull(),
        )
    }

    @Test
    fun `timeline 항목이 있는 분석 결과이면_VideoTimeline으로 변환되어 taskId와 timestamp와 description이 정확히 매핑된다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult =
            validAnalysisResult(
                timeline =
                    listOf(
                        VideoAnalysisResult.TimelineItem(timestampSeconds = 0, description = "인트로"),
                        VideoAnalysisResult.TimelineItem(timestampSeconds = 135, description = "스크램블 교차로"),
                        VideoAnalysisResult.TimelineItem(timestampSeconds = 330, description = "라멘 점심"),
                    ),
            )
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when
        listener.handle(event)

        // then - timeline이 VideoTimeline 도메인 객체로 변환되어 taskId, timestampSeconds, description이 정확히 매핑된다
        val timelinesCaptor = argumentCaptor<List<VideoTimeline>>()
        verify(videoAnalysisResultSaver).save(
            eq("s1"),
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            any(),
            timelinesCaptor.capture(),
            anyOrNull(),
        )

        val savedTimelines = timelinesCaptor.firstValue
        assertEquals(3, savedTimelines.size)
        savedTimelines.forEach { assertEquals("s1", it.videoAnalysisTaskId) }
        assertEquals(0, savedTimelines[0].timestampSeconds)
        assertEquals("인트로", savedTimelines[0].description)
        assertEquals(135, savedTimelines[1].timestampSeconds)
        assertEquals("스크램블 교차로", savedTimelines[1].description)
        assertEquals(330, savedTimelines[2].timestampSeconds)
        assertEquals("라멘 점심", savedTimelines[2].description)
    }

    @Test
    fun `timeline이 비어있는 분석 결과이면_빈 리스트로 save에 전달된다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult = validAnalysisResult(timeline = emptyList())
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        // when
        listener.handle(event)

        // then - 빈 리스트가 전달된다
        val timelinesCaptor = argumentCaptor<List<VideoTimeline>>()
        verify(videoAnalysisResultSaver).save(
            eq("s1"),
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            any(),
            timelinesCaptor.capture(),
            anyOrNull(),
        )
        assertEquals(0, timelinesCaptor.firstValue.size)
    }

    @Test
    fun `장소 보강 중 예외가 발생해도_분석 결과는 COMPLETED 상태로 유지되고_알림이 전송된다`() {
        // given - 분석은 성공하지만, 장소 보강에서 예외 발생
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())
        whenever(placeEnrichService.enrichPlaces("s1", "도쿄"))
            .thenThrow(RuntimeException("Google Places API 오류"))
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())

        // when - 영상 분석을 실행한다
        listener.handle(event)

        // then - 분석 결과는 저장되고, 장소 보강 실패에도 알림이 전송된다
        verify(videoAnalysisResultSaver).save(
            any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull(),
        )
        verify(videoAnalysisNotificationPort).notifyAnalysisComplete(any(), any())
        // updateStatus(FAILED)는 호출되지 않는다
        verify(videoAnalysisTaskPersistencePort, never()).updateStatus(any(), any())
    }

    @Test
    fun `여행 계획 자동 생성 시 3개 요청 중 1개가 실패하면_성공한 2개만 processed 표시되고_전체 저장된다`() {
        // given - 미처리 요청 3개, 두 번째에서 예외 발생
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        val request1 = TripPlanRequest.create("member-1", "s1")
        val request2 = TripPlanRequest.create("member-2", "s1")
        val request3 = TripPlanRequest.create("member-3", "s1")
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(listOf(request1, request2, request3))

        var callCount = 0
        whenever(tripPlanService.createFromAnalysisIfAbsent(any(), any(), any())).thenAnswer {
            callCount++
            if (callCount == 2) throw RuntimeException("DB 오류")
        }
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(listOf("member-1", "member-3"))

        // when
        listener.handle(event)

        // then - 성공한 요청만 processed, 실패한 요청은 미처리, 전체 saveAll 호출
        assertEquals(true, request1.processed)
        assertEquals(false, request2.processed)
        assertEquals(true, request3.processed)
        verify(tripPlanRequestPort).saveAll(listOf(request1, request2, request3))
    }

    @Test
    fun `title과 destination이 모두 null인 분석 결과이면_여행 계획 제목이 기본값으로 생성된다`() {
        // given - title과 destination이 모두 null인 분석 결과
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult = validAnalysisResult(destination = null, title = null)
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(analysisResult)

        val request = TripPlanRequest.create("member-1", "s1")
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(listOf(request))
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(listOf("member-1"))

        // when
        listener.handle(event)

        // then - 여행 계획 제목이 "여행 계획" 기본값으로 전달된다
        verify(tripPlanService).createFromAnalysisIfAbsent("member-1", "s1", "여행 계획")
    }

    private fun validAnalysisResult(
        destination: String? = "도쿄",
        summary: String? = null,
        title: String? = "도쿄 3박 4일 여행",
        timeline: List<VideoAnalysisResult.TimelineItem> = emptyList(),
    ) = VideoAnalysisResult(
        valid = true,
        destination = destination,
        title = title,
        summary = summary,
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
        timeline = timeline,
    )
}
