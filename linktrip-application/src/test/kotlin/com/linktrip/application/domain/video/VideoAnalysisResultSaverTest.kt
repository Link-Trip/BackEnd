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
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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

    @Test
    fun `summary와 비용 정보가 포함된 분석 결과를 저장하면_updateValidAndStatus에 메타데이터가 전달된다`() {
        // when - summary, 비용, destination 포함하여 저장한다
        saver.save(
            videoAnalysisTaskId = "task-1",
            itineraryItems = emptyList(),
            summary = "도쿄 3박 4일 여행 요약",
            estimatedMinCost = 800000,
            estimatedMaxCost = 1200000,
            costBasis = CostBasis.VIDEO_MENTIONED,
            destination = "도쿄, 일본",
        )

        // then - 메타데이터가 정확히 전달된다
        verify(videoAnalysisTaskPersistencePort).updateValidAndStatus(
            eq("task-1"),
            eq(true),
            eq(VideoAnalysisTaskStatus.COMPLETED),
            eq("도쿄 3박 4일 여행 요약"),
            eq(800000L),
            eq(1200000L),
            eq(CostBasis.VIDEO_MENTIONED),
            eq("도쿄, 일본"),
        )
    }

    @Test
    fun `해시태그가 포함된 분석 결과를 저장하면_해시태그가 조회되고_신규 해시태그는 생성된다`() {
        // given - 기존에 "맛집여행" 해시태그만 존재하는 상태
        val existingHashtag = Hashtag(id = "h1", name = "맛집여행")
        whenever(hashtagPersistencePort.findByNames(listOf("맛집여행", "문화탐방")))
            .thenReturn(listOf(existingHashtag))

        val newHashtag = Hashtag(id = "h2", name = "문화탐방")
        whenever(hashtagPersistencePort.saveAll(any())).thenReturn(listOf(newHashtag))

        // when - 해시태그 2개 포함하여 저장한다
        saver.save(
            videoAnalysisTaskId = "task-1",
            itineraryItems = emptyList(),
            hashtags = listOf("맛집여행", "문화탐방"),
        )

        // then - 기존 해시태그를 조회하고, 신규 해시태그를 생성하고, 연결 관계를 저장한다
        verify(hashtagPersistencePort).findByNames(listOf("맛집여행", "문화탐방"))
        verify(hashtagPersistencePort).saveAll(any())
        verify(hashtagPersistencePort).saveAllTaskHashtags(any())
    }

    @Test
    fun `해시태그가 비어있으면_해시태그 관련 저장을 수행하지 않는다`() {
        // when - 해시태그 없이 저장한다
        saver.save("task-1", emptyList(), hashtags = emptyList())

        // then - 해시태그 관련 메서드가 호출되지 않는다
        verify(hashtagPersistencePort, never()).findByNames(any())
        verify(hashtagPersistencePort, never()).saveAll(any())
        verify(hashtagPersistencePort, never()).saveAllTaskHashtags(any())
    }
}
