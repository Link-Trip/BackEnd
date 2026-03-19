package com.linktrip.application.domain.video

import com.linktrip.application.Fixtures
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import com.linktrip.common.config.event.Events
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.set
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    lateinit var videoSummaryPersistencePort: VideoSummaryPersistencePort

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
    fun `신규 YouTube URL로 분석 요청하면_VideoSummary를 PENDING 상태로 저장하고_분석 이벤트를 발행한다`() {
        // given - 아직 분석된 적 없는 새로운 YouTube URL
        val url = "https://www.youtube.com/watch?v=test123"
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(null)
        val saved = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.PENDING)
            .sample()
        whenever(videoSummaryPersistencePort.save(any())).thenReturn(saved)

        // when - 영상 분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - PENDING 상태로 저장되고, 올바른 초기 상태의 VideoSummary가 저장되며, 분석 이벤트가 발행된다
        assertEquals(VideoSummaryStatus.PENDING, result.status)
        assertEquals(url, result.youtubeUrl)

        val saveCaptor = argumentCaptor<VideoSummary>()
        verify(videoSummaryPersistencePort).save(saveCaptor.capture())
        val savedArg = saveCaptor.firstValue
        assertEquals(url, savedArg.youtubeUrl)
        assertEquals(VideoSummaryStatus.PENDING, savedArg.status)
        assertFalse(savedArg.valid)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals(saved.id, eventCaptor.firstValue.videoSummaryId)
        assertEquals(url, eventCaptor.firstValue.youtubeUrl)
    }

    @Test
    fun `이미 분석 중인 PENDING 상태의 URL로 요청하면_기존 VideoSummary를 그대로 반환하고_저장이나 이벤트 발행을 하지 않는다`() {
        // given - 이미 PENDING 상태로 분석 중인 URL
        val url = "https://youtube.com/watch?v=existing"
        val existing = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.PENDING)
            .sample()
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(existing)

        // when - 같은 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - 기존 객체를 그대로 반환하고, save/updateStatus/이벤트 모두 호출되지 않는다
        assertEquals(existing.id, result.id)
        assertEquals(existing.youtubeUrl, result.youtubeUrl)
        assertEquals(VideoSummaryStatus.PENDING, result.status)
        verify(videoSummaryPersistencePort, never()).save(any())
        verify(videoSummaryPersistencePort, never()).updateStatus(any(), any())
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `분석 완료된 COMPLETED 상태의 URL로 요청하면_기존 결과를 반환하고_재분석하지 않는다`() {
        // given - 이미 분석이 완료된 URL
        val url = "https://youtube.com/watch?v=completed"
        val existing = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.COMPLETED)
            .sample()
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(existing)

        // when - 완료된 URL로 다시 분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - 기존 완료된 결과를 그대로 반환하고, 재분석하지 않는다
        assertEquals(VideoSummaryStatus.COMPLETED, result.status)
        assertEquals(existing.id, result.id)
        verify(videoSummaryPersistencePort, never()).save(any())
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `유효하지 않은 영상으로 판정된 INVALID URL로 요청하면_기존 결과를 그대로 반환한다`() {
        // given - 이전에 유효하지 않은 영상으로 판정된 URL
        val url = "https://youtube.com/watch?v=invalid"
        val existing = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.INVALID)
            .sample()
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(existing)

        // when - INVALID 상태의 URL로 다시 요청한다
        val result = service.analyzeVideo(url)

        // then - INVALID 상태 그대로 반환하고, 이벤트를 발행하지 않는다
        assertEquals(VideoSummaryStatus.INVALID, result.status)
        verify(mockPublisher, never()).publishEvent(any<Any>())
    }

    @Test
    fun `이전에 실패한 FAILED 상태의 URL로 요청하면_PENDING으로 상태를 변경하고_재분석 이벤트를 발행한다`() {
        // given - 이전 분석이 실패한 URL
        val url = "https://youtube.com/watch?v=failed"
        val existing = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("id", "failed-id")
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.FAILED)
            .sample()
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(existing)

        // when - 실패한 URL로 재분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - PENDING으로 상태가 변경되고, 재분석 이벤트에 기존 ID와 URL이 담긴다
        assertEquals(VideoSummaryStatus.PENDING, result.status)
        assertEquals("failed-id", result.id)
        assertEquals(url, result.youtubeUrl)

        verify(videoSummaryPersistencePort).updateStatus("failed-id", VideoSummaryStatus.PENDING)

        val eventCaptor = argumentCaptor<VideoAnalyzeEvent>()
        verify(mockPublisher).publishEvent(eventCaptor.capture())
        assertEquals("failed-id", eventCaptor.firstValue.videoSummaryId)
        assertEquals(url, eventCaptor.firstValue.youtubeUrl)
    }

    @Test
    fun `YouTube 도메인이 아닌 URL로 분석 요청하면_INVALID_YOUTUBE_URL 예외가 발생한다`() {
        // given - YouTube가 아닌 URL

        // when & then - INVALID_YOUTUBE_URL 예외가 발생한다
        val exception = assertThrows<LinktripException> {
            service.analyzeVideo("https://naver.com/video")
        }
        assertEquals(ExceptionCode.INVALID_YOUTUBE_URL.statusCode, exception.statusCode)
    }

    @Test
    fun `vimeo나 빈 문자열 등 YouTube가 아닌 URL은 모두 거부된다`() {
        // given - YouTube가 아닌 다양한 URL들

        // when & then - 모두 INVALID_YOUTUBE_URL 예외가 발생한다
        assertThrows<LinktripException> { service.analyzeVideo("https://vimeo.com/123") }
        assertThrows<LinktripException> { service.analyzeVideo("not-a-url") }
        assertThrows<LinktripException> { service.analyzeVideo("") }
    }

    @Test
    fun `youtu_be 단축 URL로 분석 요청해도_정상적으로 VideoSummary가 생성된다`() {
        // given - youtu.be 단축 URL
        val url = "https://youtu.be/test123"
        whenever(videoSummaryPersistencePort.findByYoutubeUrl(url)).thenReturn(null)
        val saved = Fixtures.monkey.giveMeBuilder<VideoSummary>()
            .set("youtubeUrl", url)
            .set("status", VideoSummaryStatus.PENDING)
            .sample()
        whenever(videoSummaryPersistencePort.save(any())).thenReturn(saved)

        // when - youtu.be URL로 분석을 요청한다
        val result = service.analyzeVideo(url)

        // then - 정상적으로 PENDING 상태의 VideoSummary가 생성된다
        assertEquals(VideoSummaryStatus.PENDING, result.status)
        assertEquals(url, result.youtubeUrl)
    }
}
