package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class DiscoverVideoServiceTest {
    @Mock
    lateinit var youTubeVideoPersistencePort: YouTubeVideoPersistencePort

    @InjectMocks
    lateinit var service: DiscoverVideoService

    @Test
    fun `전체 영상 조회 요청을_YouTubeVideoPersistencePort에 위임하여 결과를 반환한다`() {
        // given - DB에 영상 2개가 존재하는 상태
        val videos = listOf(createVideo("v1"), createVideo("v2"))
        whenever(youTubeVideoPersistencePort.findAll()).thenReturn(videos)

        // when - 전체 영상 조회를 요청한다
        val result = service.getVideos()

        // then - 2개의 영상이 반환된다
        assertEquals(2, result.size)
        assertEquals("v1", result[0].videoId)
    }

    @Test
    fun `국가별 영상 조회 요청을_해당 국가 필터로 위임하여 결과를 반환한다`() {
        // given - "일본" 국가의 영상 1개가 존재하는 상태
        val videos = listOf(createVideo("v1"))
        whenever(youTubeVideoPersistencePort.findAllByCountry("일본")).thenReturn(videos)

        // when - "일본" 국가별 영상을 조회한다
        val result = service.getVideosByCountry("일본")

        // then - 1개의 영상이 반환된다
        assertEquals(1, result.size)
    }

    @Test
    fun `지역별 영상 조회 요청을_해당 지역 필터로 위임하여 결과를 반환한다`() {
        // given - "아시아" 지역의 영상 1개가 존재하는 상태
        val videos = listOf(createVideo("v1"))
        whenever(youTubeVideoPersistencePort.findAllByRegion("아시아")).thenReturn(videos)

        // when - "아시아" 지역별 영상을 조회한다
        val result = service.getVideosByRegion("아시아")

        // then - 1개의 영상이 반환된다
        assertEquals(1, result.size)
    }

    @Test
    fun `테마별 영상 조회 시_커서 기반 페이지네이션으로 결과를 반환한다`() {
        // given - "미식" 테마의 영상이 커서 기반으로 조회되는 상태
        val cursor = LocalDateTime.of(2024, 1, 1, 0, 0)
        val page =
            CursorPage(
                items = listOf(createVideo("v1")),
                nextCursor = "next",
                hasNext = true,
            )
        whenever(youTubeVideoPersistencePort.findAllByTheme("미식", cursor, 10)).thenReturn(page)

        // when - 커서와 함께 테마별 영상을 조회한다
        val result = service.getVideosByTheme("미식", cursor, 10)

        // then - 1개의 영상과 다음 페이지 정보가 반환된다
        assertEquals(1, result.items.size)
        assertEquals(true, result.hasNext)
    }

    private fun createVideo(videoId: String) =
        YouTubeVideoDetail(
            id = "id-$videoId",
            videoId = videoId,
            title = "title",
            description = "desc",
            thumbnailUrl = "thumb",
            channelId = "ch1",
            channelTitle = "channel",
            viewCount = 1000,
            likeCount = 100,
            duration = "PT10M",
            publishedAt = "2024-01-01",
            region = "아시아",
            country = "일본",
            city = "도쿄",
            theme = null,
        )
}
