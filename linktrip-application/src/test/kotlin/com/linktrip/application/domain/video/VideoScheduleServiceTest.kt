package com.linktrip.application.domain.video

import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.VideoAnalysisTaskPersistencePort
import com.linktrip.application.port.output.persistence.VideoTimelinePersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class VideoScheduleServiceTest {
    @Mock
    lateinit var videoAnalysisTaskPersistencePort: VideoAnalysisTaskPersistencePort

    @Mock
    lateinit var travelItineraryItemPersistencePort: TravelItineraryItemPersistencePort

    @Mock
    lateinit var videoTimelinePersistencePort: VideoTimelinePersistencePort

    @InjectMocks
    lateinit var service: VideoScheduleService

    @Test
    fun `존재하는 영상 ID로 일정을 조회하면_VideoAnalysisTask와 일정 항목과 타임라인 목록을 함께 반환한다`() {
        // given - DB에 존재하는 VideoAnalysisTask, 일정 항목, 타임라인
        val task =
            VideoAnalysisTask(
                id = "s1",
                youtubeUrl = "https://youtube.com/1",
                valid = true,
                status = VideoAnalysisTaskStatus.COMPLETED,
            )
        val items =
            listOf(
                TravelItineraryItem(
                    id = "i1",
                    videoAnalysisTaskId = "s1",
                    day = 1,
                    itemOrder = 1,
                    category = Category.EAT,
                    name = "맛집",
                    description = null,
                    tips = null,
                ),
            )
        val timelines =
            listOf(
                VideoTimeline.create("s1", 0, "인트로"),
                VideoTimeline.create("s1", 135, "스크램블 교차로"),
            )
        whenever(videoAnalysisTaskPersistencePort.findById("s1")).thenReturn(task)
        whenever(travelItineraryItemPersistencePort.findByVideoAnalysisTaskIdWithPlace("s1")).thenReturn(items)
        whenever(videoTimelinePersistencePort.findByVideoAnalysisTaskId("s1")).thenReturn(timelines)

        // when - 영상 ID로 일정을 조회한다
        val result = service.getVideoSchedule("s1")

        // then - VideoAnalysisTask, 일정 항목, 타임라인을 함께 반환한다
        assertEquals(task, result.videoAnalysisTask)
        assertEquals(1, result.items.size)
        assertEquals(2, result.timelines.size)
        assertEquals(0, result.timelines[0].timestampSeconds)
        assertEquals("인트로", result.timelines[0].description)
        assertEquals(135, result.timelines[1].timestampSeconds)
        assertEquals("스크램블 교차로", result.timelines[1].description)
    }

    @Test
    fun `존재하지 않는 영상 ID로 일정을 조회하면_NOT_FOUND 예외가 발생한다`() {
        // given - DB에 존재하지 않는 ID
        whenever(videoAnalysisTaskPersistencePort.findById("not-exist")).thenReturn(null)

        // when - 존재하지 않는 ID로 조회한다
        val exception =
            assertThrows<LinktripException> {
                service.getVideoSchedule("not-exist")
            }

        // then - NOT_FOUND 예외가 발생한다
        assertEquals(ExceptionCode.NOT_FOUND_VIDEO_ANALYSIS_TASK.statusCode, exception.statusCode)
    }
}
