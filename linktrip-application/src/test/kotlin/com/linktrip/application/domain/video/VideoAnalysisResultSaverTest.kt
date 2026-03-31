package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.HashtagPersistencePort
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.persistence.VideoTimelinePersistencePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class VideoAnalysisResultSaverTest {
    @Mock
    lateinit var travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort

    @Mock
    lateinit var videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort

    @Mock
    lateinit var videoTimelinePersistencePort: VideoTimelinePersistencePort

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
                    videoAnalysisTaskId = "task-1",
                    day = 1,
                    itemOrder = 1,
                    category = Category.EAT,
                    name = "맛집",
                    description = null,
                    tips = null,
                ),
            )

        // when - 분석 결과를 저장한다
        saver.save("task-1", items)

        // then - 일정 항목이 저장되고, VideoAnalysisTask가 COMPLETED로 변경된다
        verify(travelItineraryItemPersistencePort).saveAll(items)
        verify(videoAnalysisTaskPersistencePort).updateValidAndStatus(
            eq("task-1"),
            eq(true),
            eq(VideoAnalysisTaskStatus.COMPLETED),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `타임라인이 있는 분석 결과를 저장하면_타임라인도 DB에 함께 저장된다`() {
        // given - 타임라인이 포함된 저장 요청
        val timelines =
            listOf(
                VideoTimeline.create("task-1", 0, "인트로"),
                VideoTimeline.create("task-1", 135, "시부야 스크램블 교차로"),
            )

        // when - 타임라인과 함께 저장한다
        saver.save("task-1", emptyList(), timelines = timelines)

        // then - 타임라인이 DB에 저장된다
        verify(videoTimelinePersistencePort).saveAll(timelines)
    }

    @Test
    fun `타임라인이 없는 분석 결과를 저장하면_빈 리스트로 타임라인 저장이 호출된다`() {
        // when - 타임라인 없이 저장한다
        saver.save("task-1", emptyList())

        // then - 빈 리스트로 타임라인 저장이 호출된다
        verify(videoTimelinePersistencePort).saveAll(emptyList())
    }
}
