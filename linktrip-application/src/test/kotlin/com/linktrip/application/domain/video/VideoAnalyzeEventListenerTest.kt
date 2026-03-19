package com.linktrip.application.domain.video

import com.linktrip.application.port.output.external.VideoAnalysisNotificationPort
import com.linktrip.application.port.output.external.VideoAnalyzePort
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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
    lateinit var videoSummaryPersistencePort: VideoSummaryPersistencePort

    @Mock
    lateinit var videoAnalysisResultSaver: VideoAnalysisResultSaver

    @Mock
    lateinit var placeEnrichService: PlaceEnrichService

    @Mock
    lateinit var videoAnalysisNotificationPort: VideoAnalysisNotificationPort

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
        inOrder.verify(videoAnalysisResultSaver).save(eq("s1"), any())
        inOrder.verify(placeEnrichService).enrichPlaces("s1", "도쿄")
        inOrder.verify(videoAnalysisNotificationPort).notifyAnalysisComplete("s1")
    }

    @Test
    fun `여행 영상이 아닌 것으로 판정되면_INVALID 상태로 변경하고_일정 저장과 장소 보강을 수행하지 않는다`() {
        // given - 여행 영상이 아닌 것으로 판정된 분석 결과
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val invalidResult = VideoAnalysisResult(valid = false, destination = null, days = emptyList())
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenReturn(invalidResult)

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - INVALID 상태로 변경하고, 저장과 보강은 수행하지 않는다
        verify(videoSummaryPersistencePort).updateValidAndStatus("s1", valid = false, VideoSummaryStatus.INVALID)
        verify(videoAnalysisResultSaver, never()).save(any(), any())
        verify(placeEnrichService, never()).enrichPlaces(any(), any())
        verify(videoAnalysisNotificationPort, never()).notifyAnalysisComplete(any())
    }

    @Test
    fun `AI 분석 중 예외가 발생하면_FAILED 상태로 변경하고_이후 단계를 모두 건너뛴다`() {
        // given - AI 분석 시 예외가 발생하는 상황
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        whenever(videoAnalyzePort.analyze("https://youtube.com/1")).thenThrow(RuntimeException("AI 오류"))

        // when - 이벤트를 처리한다
        listener.handle(event)

        // then - FAILED 상태로 변경하고, 결과 저장은 수행하지 않는다
        verify(videoSummaryPersistencePort).updateStatus("s1", VideoSummaryStatus.FAILED)
        verify(videoAnalysisResultSaver, never()).save(any(), any())
    }

    @Test
    fun `2일치 3개 일정 항목이 있는 분석 결과를_VideoScheduleItem으로 변환하면_day와 category와 순서가 정확히 매핑된다`() {
        // given - 2일치 3개 일정 항목이 포함된 분석 결과
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")
        val analysisResult =
            VideoAnalysisResult(
                valid = true,
                destination = "파리",
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
        val captor = org.mockito.kotlin.argumentCaptor<List<VideoScheduleItem>>()
        verify(videoAnalysisResultSaver).save(eq("s1"), captor.capture())

        val savedItems = captor.firstValue
        assert(savedItems.size == 3) { "Expected 3 items but got ${savedItems.size}" }
        assert(savedItems[0].day == 1 && savedItems[0].name == "에펠탑")
        assert(savedItems[1].day == 1 && savedItems[1].name == "카페")
        assert(savedItems[2].day == 2 && savedItems[2].name == "샹젤리제")
    }
}
