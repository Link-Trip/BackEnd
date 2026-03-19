package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.VideoScheduleItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoSummaryPersistencePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class VideoAnalysisResultSaverTest {
    @Mock
    lateinit var videoScheduleItemPersistencePort: VideoScheduleItemPersistencePort

    @Mock
    lateinit var videoSummaryPersistencePort: VideoSummaryPersistencePort

    @InjectMocks
    lateinit var saver: VideoAnalysisResultSaver

    @Test
    fun `분석 결과를 저장하면_일정 항목들이 DB에 저장되고_VideoSummary 상태가 COMPLETED로 변경된다`() {
        // given - 저장할 일정 항목 목록
        val items =
            listOf(
                VideoScheduleItem(
                    id = "item-1",
                    videoSummaryId = "summary-1",
                    day = 1,
                    itemOrder = 1,
                    category = Category.EAT,
                    name = "맛집",
                    description = null,
                    tips = null,
                ),
            )

        // when - 분석 결과를 저장한다
        saver.save("summary-1", items)

        // then - 일정 항목이 저장되고, VideoSummary가 COMPLETED로 변경된다
        verify(videoScheduleItemPersistencePort).saveAll(items)
        verify(videoSummaryPersistencePort).updateValidAndStatus(
            "summary-1",
            valid = true,
            VideoSummaryStatus.COMPLETED,
        )
    }
}
