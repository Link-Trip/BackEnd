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
    fun `신규 YouTube URL로 분석 요청하면_VideoAnalysisTask를 PENDING 상태로 저장하고_분석 이벤트를 발행한다`() {
        // given - 아직 분석된 적 없는 새로운 YouTube URL
        val url = "https://www.youtube.com/watch?v=test123"
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(url)).thenReturn(null)
        val saved =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", url)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.save(any())).thenReturn(saved)

        // when - 영상 분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - PENDING 상태로 저장되고, 올바른 초기 상태의 VideoAnalysisTask가 저장되며, 분석 이벤트가 발행된다
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals(url, result.youtubeUrl)

        val saveCaptor = argumentCaptor<VideoAnalysisTask>()
        verify(videoAnalysisTaskPersistencePort).save(saveCaptor.capture())
        val savedArg = saveCaptor.firstValue
        assertEquals(url, savedArg.youtubeUrl)
        assertEquals(VideoAnalysisTaskStatus.PENDING, savedArg.status)
        assertFalse(savedArg.valid)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals(saved.id, eventCaptor.firstValue.videoAnalysisTaskId)
        assertEquals(url, eventCaptor.firstValue.youtubeUrl)
    }

    @Test
    fun `이미 분석 중인 PENDING 상태의 URL로 요청하면_기존 VideoAnalysisTask를 그대로 반환하고_저장이나 이벤트 발행을 하지 않는다`() {
        // given - 이미 PENDING 상태로 분석 중인 URL (정규화된 형태)
        val normalizedUrl = "https://www.youtube.com/watch?v=existing"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.PENDING)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 같은 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(normalizedUrl)

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
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 완료된 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(normalizedUrl)

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
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - INVALID 상태의 URL로 다시 요청한다
        val result = service.analyzeVideo(normalizedUrl)

        // then - INVALID 상태 그대로 반환하고, 이벤트를 발행하지 않는다
        assertEquals(VideoAnalysisTaskStatus.INVALID, result.status)
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `이전에 실패한 FAILED 상태의 URL로 요청하면_PENDING으로 상태를 변경하고_재분석 이벤트를 발행한다`() {
        // given - 이전 분석이 실패한 URL (정규화된 형태)
        val normalizedUrl = "https://www.youtube.com/watch?v=failed"
        val existing =
            Fixtures.monkey.giveMeBuilder<VideoAnalysisTask>()
                .set("id", "failed-id")
                .set("youtubeUrl", normalizedUrl)
                .set("status", VideoAnalysisTaskStatus.FAILED)
                .sample()
        whenever(videoAnalysisTaskPersistencePort.findByYoutubeUrl(normalizedUrl)).thenReturn(existing)

        // when - 실패한 URL로 재분석을 요청한다
        val result = service.analyzeVideo(normalizedUrl)

        // then - PENDING으로 상태가 변경되고, 재분석 이벤트에 기존 ID와 URL이 담긴다
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals("failed-id", result.id)
        assertEquals(normalizedUrl, result.youtubeUrl)

        verify(videoAnalysisTaskPersistencePort).updateStatus("failed-id", VideoAnalysisTaskStatus.PENDING)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals("failed-id", eventCaptor.firstValue.videoAnalysisTaskId)
        assertEquals(normalizedUrl, eventCaptor.firstValue.youtubeUrl)
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
                .sample()
        whenever(videoAnalysisTaskPersistencePort.save(any())).thenReturn(saved)

        // when - youtu.be URL로 분석을 요청한다
        val result = service.analyzeVideo(shortUrl)

        // then - 정규화된 URL로 PENDING 상태의 VideoAnalysisTask가 생성된다
        assertEquals(VideoAnalysisTaskStatus.PENDING, result.status)
        assertEquals(normalizedUrl, result.youtubeUrl)
    }
}
