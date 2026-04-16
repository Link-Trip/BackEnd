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
    fun `이벤트를 수신하면_큐에 enqueue만 수행한다`() {
        // given
        val event = VideoAnalyzeEvent("s1", "https://youtube.com/1")

        // when
        listener.handle(event)

        // then - 큐에 넣기만 하고 직접 분석하지 않는다
        verify(videoAnalysisQueuePort).enqueue("s1", "https://youtube.com/1")
    }
}
