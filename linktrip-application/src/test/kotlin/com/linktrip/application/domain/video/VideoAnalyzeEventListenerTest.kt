package com.linktrip.application.domain.video

import com.linktrip.application.port.output.queue.VideoAnalysisQueuePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class VideoAnalyzeEventListenerTest {
    @Mock
    lateinit var videoAnalysisQueuePort: VideoAnalysisQueuePort

    @InjectMocks
    lateinit var listener: VideoAnalyzeEventListener

    @Test
    fun `USER 이벤트를 수신하면_큐에 USER source 로 enqueue 한다`() {
        // given - 사용자 직접 요청에서 발생한 이벤트
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1", Source.USER)

        // when
        listener.handle(event)

        // then - event 의 source 가 그대로 큐로 전달되어야 USER 우선순위가 보장된다
        verify(videoAnalysisQueuePort).enqueue("s1", "https://youtube.com/1", Source.USER)
    }

    @Test
    fun `BATCH 이벤트를 수신하면_큐에 BATCH source 로 enqueue 한다`() {
        // given - 배치/수집에서 발생한 이벤트
        val event = VideoAnalyzeEvent("s2", "https://youtube.com/2", Source.BATCH)

        // when
        listener.handle(event)

        // then - BATCH source 가 보존되어 사용자 요청보다 후순위로 처리됨
        verify(videoAnalysisQueuePort).enqueue("s2", "https://youtube.com/2", Source.BATCH)
    }
}
