package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.HashtagPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class VideoAnalysisResultSaverTest {
    @Mock
    lateinit var travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort

    @Mock
    lateinit var videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort

    @Mock
    lateinit var hashtagPersistencePort: HashtagPersistencePort

    @InjectMocks
    lateinit var saver: VideoAnalysisResultSaver

    @Test
    fun `분석 결과를 저장하면_일정 항목들이 DB에 저장되고_VideoAnalysisTask 상태가 COMPLETED로 변경된다`() {
        // given - 저장할 일정 항목 목록
        val items =
            listOf(
                TravelItineraryItem(
                    id = "item-1",
                    videoAnalysisTaskId = "summary-1",
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

        // then - 일정 항목이 저장되고, VideoAnalysisTask가 COMPLETED로 변경된다
        verify(travelItineraryItemPersistencePort).saveAll(items)
        verify(videoAnalysisTaskPersistencePort).updateValidAndStatus(
            "summary-1",
            valid = true,
            VideoAnalysisTaskStatus.COMPLETED,
        )
    }
}
