package com.linktrip.application.domain.video

import com.linktrip.application.domain.youtube.SearchKeywordLoader
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.external.YouTubePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class KeywordAnalyzeServiceTest {
    @Mock
    lateinit var youTubePort: YouTubePort

    @Mock
    lateinit var videoAnalyzeUseCase: VideoAnalyzeUseCase

    @InjectMocks
    lateinit var service: KeywordAnalyzeService

    @Test
    fun `존재하지 않는 region 으로 호출하면_외부 API 호출 없이 빈 결과를 반환한다 (불필요한 비용 회피)`() {
        // 가드 의도: 필터 결과 0 이면 외부 API 가 한 번도 호출되지 않아야 함.
        val result = service.analyzeByKeywords(region = "존재하지-않는-region-xyz", country = null, maxResults = 5)

        assertEquals(0, result.keywordCount)
        assertEquals(0, result.tasks.size)
        verify(youTubePort, never()).searchVideos(any(), any())
        verify(videoAnalyzeUseCase, never()).analyzeVideo(any(), any())
    }

    @Test
    fun `region 과 country 모두 null 이면_전체 키워드 풀에 대해 검색을 시도한다`() {
        val totalKeywords = SearchKeywordLoader.getAll().size
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(emptyList())

        val result = service.analyzeByKeywords(region = null, country = null, maxResults = 5)

        assertEquals(totalKeywords, result.keywordCount)
        verify(youTubePort, times(totalKeywords)).searchVideos(any(), any())
    }

    @Test
    fun `YouTube 검색이 일부 키워드에서 예외를 던져도_나머지 키워드는 계속 처리된다 (best-effort)`() {
        // 키워드 단위 실패 시 전체 잡이 멈추면 발견된 신규 영상이 모두 누락. 키워드 단위로 격리해야 함.
        var callIndex = 0
        whenever(youTubePort.searchVideos(any(), any())).thenAnswer {
            callIndex++
            if (callIndex == 1) {
                throw RuntimeException("YouTube API 일시 오류")
            }
            emptyList<YouTubeSearchResult>()
        }
        val totalKeywords = SearchKeywordLoader.getAll().size

        // 외부로 예외가 전파되지 않고 정상 종료
        val result = service.analyzeByKeywords(region = null, country = null, maxResults = 5)

        // 모든 키워드가 시도되어야 함 (한 키워드 실패가 다른 키워드 처리를 막아선 안 됨)
        verify(youTubePort, times(totalKeywords)).searchVideos(any(), any())
        // keywordCount 는 필터 통과한 키워드 수 — 검색 실패 여부와 무관하게 동일
        assertEquals(totalKeywords, result.keywordCount)
    }

    @Test
    fun `개별 영상 분석이 실패해도_같은 키워드의 다른 영상과 task 결과는 정상 보존된다 (영상 단위 격리)`() {
        // 영상 단위 실패가 키워드/잡 전체를 무너뜨리지 않게.
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(
            listOf(
                searchResult("video-1"),
                searchResult("video-2-fail"),
                searchResult("video-3"),
            ),
        )
        whenever(videoAnalyzeUseCase.analyzeVideo(any(), eq(Source.BATCH))).thenAnswer { invocation ->
            val url = invocation.arguments[0] as String
            if (url.endsWith("video-2-fail")) {
                throw RuntimeException("분석 일시 오류")
            }
            videoAnalysisTask(url)
        }
        val totalKeywords = SearchKeywordLoader.getAll().size

        val result = service.analyzeByKeywords(region = null, country = null, maxResults = 5)

        // 키워드당 3 영상 시도, 그 중 1개만 실패 → 키워드당 2 task 성공
        assertEquals(totalKeywords * 2, result.tasks.size)
        verify(videoAnalyzeUseCase, times(totalKeywords * 3)).analyzeVideo(any(), eq(Source.BATCH))
    }

    private fun searchResult(videoId: String): YouTubeSearchResult =
        YouTubeSearchResult(
            videoId = videoId,
            title = "title-$videoId",
            description = "",
            thumbnailUrl = "",
            channelId = "",
            channelTitle = "",
            publishedAt = "",
        )

    private fun videoAnalysisTask(youtubeUrl: String): VideoAnalysisTask =
        VideoAnalysisTask(
            id = "task-$youtubeUrl",
            youtubeUrl = youtubeUrl,
            valid = false,
            status = VideoAnalysisTaskStatus.PENDING,
            source = Source.BATCH,
        )
}
