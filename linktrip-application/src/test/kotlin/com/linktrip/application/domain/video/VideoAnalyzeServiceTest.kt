package com.linktrip.application.domain.video

import com.linktrip.application.Fixtures
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.common.config.event.Events
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.set
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockitoExtension::class)
class VideoAnalyzeServiceTest {
    @Mock
    lateinit var videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort

    @Mock
    lateinit var mockPublisher: ApplicationEventPublisher

    @InjectMocks
    lateinit var service: VideoAnalyzeService

    @BeforeEach
    fun setUp() {
        Events.setPublisher(mockPublisher)
    }

    @AfterEach
    fun tearDown() {
        Events.setPublisher(ApplicationEventPublisher { })
    }

    @Test
    fun `신규 USER 요청이면_USER source 로 task 가 저장되고_USER source 로 분석 이벤트가 발행된다`() {
        // given - 아직 분석된 적 없는 새로운 YouTube URL
        val url = "https://www.youtube.com/watch?v=test123"
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(url)).thenReturn(null)
        val saved =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", url)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .set("source", Source.USER)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.save(any())).thenReturn(saved)

        // when - USER source 로 영상 분석을 요청한다
        val result = service.analyzeVideo(url, Source.USER)

        // then - PENDING + source=USER 로 저장되고, 동일 source 로 이벤트가 발행된다
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals(url, result.youtubeUrl)

        val saveCaptor = argumentCaptor<VideoAnalysisTask>()
        verify(videoAnalysisTaskPersistencePort).save(saveCaptor.capture())
        assertEquals(url, saveCaptor.firstValue.youtubeUrl)
        assertEquals(VideoAnalysisTaskStatus.PENDING, saveCaptor.firstValue.status)
        assertEquals(Source.USER, saveCaptor.firstValue.source)
        assertFalse(saveCaptor.firstValue.valid)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals(saved.id, eventCaptor.firstValue.videoAnalysisTaskId)
        assertEquals(url, eventCaptor.firstValue.youtubeUrl)
        assertEquals(Source.USER, eventCaptor.firstValue.source)
    }

    @Test
    fun `신규 BATCH 요청이면_BATCH source 로 task 가 저장되어 통계 분류가 가능하다`() {
        // given - YouTube 정기 수집/backfill  스케줄러가 신규 영상을 분석 요청
        val url = "https://www.youtube.com/watch?v=batch1"
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(url)).thenReturn(null)
        val saved =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", url)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .set("source", Source.BATCH)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.save(any())).thenReturn(saved)

        // when
        service.analyzeVideo(url, Source.BATCH)

        // then - task 와 event 모두 source=BATCH (배치 통계 + 큐 우선순위 둘 다 정상)
        val saveCaptor = argumentCaptor<VideoAnalysisTask>()
        verify(videoAnalysisTaskPersistencePort).save(saveCaptor.capture())
        assertEquals(Source.BATCH, saveCaptor.firstValue.source)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals(Source.BATCH, eventCaptor.firstValue.source)
    }

    @Test
    fun `이미 분석 중인 PENDING 상태의 URL로 요청하면_기존 VideoAnalysisTask를 그대로 반환하고_저장이나 이벤트 발행을 하지 않는다`() {
        // given - 이미 PENDING 상태로 분석 중인 URL (정규화된 형태)
        val normalizedUrl = "https://www.youtube.com/watch?v=existing"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .set("source", Source.USER)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 같은 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(normalizedUrl, Source.USER)

        // then - 기존 객체를 그대로 반환하고, save/updateStatus/이벤트 모두 호출되지 않는다
        assertEquals(existing.id, result.id)
        assertEquals(normalizedUrl, result.youtubeUrl)
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        verify(videoAnalysisTaskPersistencePort, never()).save(any())
        verify(videoAnalysisTaskPersistencePort, never()).updateStatus(any(), any())
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `분석 완료된 COMPLETED 상태의 URL로 요청하면_기존 결과를 반환하고_재분석하지 않는다`() {
        // given - 이미 분석이 완료된 URL (정규화된 형태)
        val normalizedUrl = "https://www.youtube.com/watch?v=completed"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.COMPLETED)
                .set("source", Source.USER)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 완료된 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(normalizedUrl, Source.USER)

        // then - 기존 완료된 결과를 그대로 반환하고, 재분석하지 않는다
        assertEquals(VideoAnalysisTaskStatus.COMPLETED, result.status)
        assertEquals(existing.id, result.id)
        verify(videoAnalysisTaskPersistencePort, never()).save(any())
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `유효하지 않은 영상으로 판정된 INVALID URL로 요청하면_기존 결과를 그대로 반환한다`() {
        // given - 이전에 유효하지 않은 영상으로 판정된 URL (정규화된 형태)
        val normalizedUrl = "https://www.youtube.com/watch?v=invalid"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.INVALID)
                .set("source", Source.USER)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - INVALID 상태의 URL로 다시 요청한다
        val result = service.analyzeVideo(normalizedUrl, Source.USER)

        // then - INVALID 상태 그대로 반환하고, 이벤트를 발행하지 않는다
        assertEquals(VideoAnalysisTaskStatus.INVALID, result.status)
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `BATCH 로 생성된 FAILED task 에 USER 가 재요청하면_이번 재시도 이벤트는 USER source 로 발행된다`() {
        // given - 배치가 만들어둔 FAILED 영상 (예: 정기 수집이 실패한 영상). audit 상 task.source=BATCH
        val normalizedUrl = "https://www.youtube.com/watch?v=failed"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("id", "failed-id")
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.FAILED)
                .set("source", Source.BATCH)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 사용자가 직접 같은 영상을 재요청
        val result = service.analyzeVideo(normalizedUrl, Source.USER)

        // then - 상태는 PENDING 으로 전환, 재시도 이벤트는 현 trigger 인 USER 로 발행 (큐에서 USER 우선순위로 처리됨)
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals("failed-id", result.id)
        verify(videoAnalysisTaskPersistencePort).updateStatus("failed-id", VideoAnalysisTaskStatus.PENDING)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals("failed-id", eventCaptor.firstValue.videoAnalysisTaskId)
        assertEquals(normalizedUrl, eventCaptor.firstValue.youtubeUrl)
        assertEquals(Source.USER, eventCaptor.firstValue.source)
    }

    @Test
    fun `youtu_be 단축 URL로 분석 요청해도_정규화된 URL로 VideoAnalysisTask가 생성된다`() {
        // given - youtu.be 단축 URL → 정규화되면 www.youtube.com 형태
        val shortUrl = "https://youtu.be/test123"
        val normalizedUrl = "https://www.youtube.com/watch?v=test123"
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(null)
        val saved =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .set("source", Source.USER)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.save(any())).thenReturn(saved)

        // when - youtu.be URL로 분석을 요청한다
        val result = service.analyzeVideo(shortUrl, Source.USER)

        // then - 정규화된 URL로 PENDING 상태의 VideoAnalysisTask가 생성된다
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals(normalizedUrl, result.youtubeUrl)
    }
}
