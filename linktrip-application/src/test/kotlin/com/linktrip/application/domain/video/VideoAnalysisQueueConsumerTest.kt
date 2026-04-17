package com.linktrip.application.domain.video

import com.linktrip.application.domain.trip.TripPlanRequest
import com.linktrip.application.domain.trip.TripPlanService
import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import com.linktrip.application.port.output.ratelimit.RateLimitBucketStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class VideoAnalysisQueueConsumerTest {
    @Mock
    lateinit var videoAnalysisQueuePort: VideoAnalysisQueuePort

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

    @Mock
    lateinit var rateLimitBucketStore: RateLimitBucketStore

    private fun createConsumer() =
        VideoAnalysisQueueConsumer(
            videoAnalysisQueuePort = videoAnalysisQueuePort,
            videoAnalyzePort = videoAnalyzePort,
            videoAnalysisResultSaver = videoAnalysisResultSaver,
            placeEnrichService = placeEnrichService,
            videoAnalysisTaskPersistencePort = videoAnalysisTaskPersistencePort,
            videoAnalysisNotificationPort = videoAnalysisNotificationPort,
            tripPlanRequestPort = tripPlanRequestPort,
            tripPlanService = tripPlanService,
            rateLimitBucketStore = rateLimitBucketStore,
        )

    @Test
    fun `유효한 여행 영상이면_결과저장_장소보강_알림 순서로 전체 파이프라인이 실행된다`() {
        // given
        val consumer = createConsumer()
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenReturn(analysisResult)
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())

        // when - processAnalysis를 직접 호출 (리플렉션 대신 consumer 내부 메서드 테스트)
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then
        verify(videoAnalysisResultSaver).save(
            any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull(),
        )
        verify(placeEnrichService).enrichPlaces("s1", "도쿄")
        verify(videoAnalysisNotificationPort).notifyAnalysisComplete(any(), any())
    }

    @Test
    fun `여행 영상이 아닌 것으로 판정되면_INVALID 상태로 변경하고_이후 단계를 건너뛴다`() {
        // given
        val consumer = createConsumer()
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
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenReturn(invalidResult)

        // when
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then
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
        verify(videoAnalysisResultSaver, never()).save(
            any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull(),
        )
    }

    @Test
    fun `AI 분석 중 예외가 발생하면_FAILED 상태로 변경한다`() {
        // given
        val consumer = createConsumer()
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenThrow(RuntimeException("AI 오류"))

        // when
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then
        verify(videoAnalysisTaskPersistencePort).updateStatus("s1", VideoAnalysisTaskStatus.FAILED)
    }

    @Test
    fun `장소 보강 중 예외가 발생해도_분석 결과는 유지되고_알림이 전송된다`() {
        // given
        val consumer = createConsumer()
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenReturn(analysisResult)
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())
        whenever(placeEnrichService.enrichPlaces("s1", "도쿄"))
            .thenThrow(RuntimeException("Google Places API 오류"))
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())

        // when
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then - 분석 결과는 저장되고, 알림도 전송된다
        verify(videoAnalysisResultSaver).save(
            any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull(),
        )
        verify(videoAnalysisNotificationPort).notifyAnalysisComplete(any(), any())
        // processAnalysis 진입 시 PROCESSING 으로는 전환되지만, 실패(FAILED) 나 재시도(PENDING) 로는 돌아가지 않아야 한다.
        verify(videoAnalysisTaskPersistencePort).updateStatus("s1", VideoAnalysisTaskStatus.PROCESSING)
        verify(videoAnalysisTaskPersistencePort, never()).updateStatus(eq("s1"), eq(VideoAnalysisTaskStatus.FAILED))
        verify(videoAnalysisTaskPersistencePort, never()).updateStatus(eq("s1"), eq(VideoAnalysisTaskStatus.PENDING))
    }

    @Test
    fun `여행 계획 자동 생성 시 3개 요청 중 1개가 실패하면_성공한 것만 processed 된다`() {
        // given
        val consumer = createConsumer()
        val analysisResult = validAnalysisResult(destination = "도쿄")
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenReturn(analysisResult)

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
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then
        assertEquals(true, request1.processed)
        assertEquals(false, request2.processed)
        assertEquals(true, request3.processed)
        verify(tripPlanRequestPort).saveAll(listOf(request1, request2, request3))
    }

    @Test
    fun `2일치 3개 일정 항목이 있는 분석 결과를_정확히 변환하여 저장한다`() {
        // given
        val consumer = createConsumer()
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
        whenever(videoAnalyzePort.extractTranscript("https://youtube.com/1")).thenReturn("transcript")
        whenever(videoAnalyzePort.analyzeFromTranscript("transcript", "https://youtube.com/1"))
            .thenReturn(analysisResult)
        whenever(tripPlanRequestPort.findUnprocessedByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())
        whenever(tripPlanRequestPort.findMemberIdsByVideoAnalysisTaskId("s1"))
            .thenReturn(emptyList())

        // when
        val method = consumer.javaClass.getDeclaredMethod("processAnalysis", VideoAnalyzeEvent::class.java)
        method.isAccessible = true
        method.invoke(consumer, VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER))

        // then
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
        assertEquals("에펠탑", savedItems[0].name)
        assertEquals(Category.ATTRACTION, savedItems[0].category)
        assertEquals("샹젤리제", savedItems[2].name)
        assertEquals(Category.SHOPPING, savedItems[2].category)
    }

    private fun validAnalysisResult(
        destination: String? = "도쿄",
        title: String? = "도쿄 3박 4일 여행",
    ) = VideoAnalysisResult(
        valid = true,
        destination = destination,
        title = title,
        summary = null,
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
        timeline = emptyList(),
    )
}
