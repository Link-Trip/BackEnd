package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class YouTubeCollectServiceTest {
    @Mock
    lateinit var youTubePort: YouTubePort

    @Mock
    lateinit var youTubeVideoPersistencePort: YouTubeVideoPersistencePort

    @Mock
    lateinit var videoAnalyzeUseCase: VideoAnalyzeUseCase

    @InjectMocks
    lateinit var service: YouTubeCollectService

    @Test
    fun `YouTube 검색으로 신규 영상을 발견하면_키워드의 region과 country 메타데이터를 태깅하여 저장한다`() {
        // given - YouTube 검색 결과 신규 영상 1개, DB에 중복 없음
        // SearchKeyword.pickRandom()은 내부적으로 호출되므로
        // YouTubePort의 searchVideos가 호출될 때 결과를 반환하도록 설정
        val searchResult =
            YouTubeSearchResult(
                videoId = "v1",
                title = "도쿄 여행",
                description = "desc",
                thumbnailUrl = "thumb",
                channelId = "ch1",
                channelTitle = "channel",
                publishedAt = "2024-01-01",
            )
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(listOf(searchResult))
        whenever(youTubeVideoPersistencePort.findExistingVideoIds(any())).thenReturn(emptySet())
        whenever(videoAnalyzeUseCase.analyzeVideo(any())).thenReturn(
            VideoAnalysisTask.create("https://www.youtube.com/watch?v=v1"),
        )

        val videoDetail =
            YouTubeVideoMeta(
                id = "id-1",
                videoId = "v1",
                title = "도쿄 여행",
                description = "desc",
                thumbnailUrl = "thumb",
                channelId = "ch1",
                channelTitle = "channel",
                viewCount = 1000,
                likeCount = 100,
                duration = "PT10M",
                publishedAt = "2024-01-01",
                region = "",
                country = "",
                city = null,
                theme = null,
            )
        whenever(youTubePort.getVideoDetails(any())).thenReturn(listOf(videoDetail))

        // when - 영상 수집을 실행한다
        service.collectVideos()

        // then - 메타데이터가 태깅되어 저장된다
        verify(youTubeVideoPersistencePort).saveAll(any())
        verify(videoAnalyzeUseCase, times(5)).analyzeVideo("https://www.youtube.com/watch?v=v1")
    }

    @Test
    fun `검색된 영상이 모두 DB에 이미 존재하면_저장을 수행하지 않는다`() {
        // given - 검색 결과의 videoId가 이미 DB에 존재하는 상태
        val searchResult =
            YouTubeSearchResult(
                videoId = "v1",
                title = "title",
                description = "desc",
                thumbnailUrl = "thumb",
                channelId = "ch1",
                channelTitle = "channel",
                publishedAt = "2024-01-01",
            )
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(listOf(searchResult))
        whenever(youTubeVideoPersistencePort.findExistingVideoIds(any())).thenReturn(setOf("v1"))

        // when - 영상 수집을 실행한다
        service.collectVideos()

        // then - 모두 중복이므로 저장을 수행하지 않는다
        verify(youTubeVideoPersistencePort, never()).saveAll(any())
        verify(videoAnalyzeUseCase, never()).analyzeVideo(any())
    }

    @Test
    fun `키워드 검색 결과가 비어있으면_상세 조회와 저장을 모두 건너뛴다`() {
        // given - 모든 키워드의 검색 결과가 비어있는 상태
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(emptyList())

        // when - 영상 수집을 실행한다
        service.collectVideos()

        // then - 상세 조회와 저장을 모두 건너뛴다
        verify(youTubePort, never()).getVideoDetails(any())
        verify(youTubeVideoPersistencePort, never()).saveAll(any())
        verify(videoAnalyzeUseCase, never()).analyzeVideo(any())
    }

    @Test
    fun `5개 키워드 중 첫 번째에서 API 오류가 발생해도_나머지 4개 키워드는 정상 처리된다`() {
        // given - 첫 호출은 예외, 나머지는 빈 결과
        var callCount = 0
        whenever(youTubePort.searchVideos(any(), any())).thenAnswer {
            callCount++
            if (callCount == 1) throw RuntimeException("API 오류")
            emptyList<YouTubeSearchResult>()
        }

        // when - 영상 수집을 실행한다
        service.collectVideos()

        // then - 예외가 전파되지 않고, 5개 키워드 모두 searchVideos가 호출된다
        verify(youTubePort, times(5)).searchVideos(any(), any())
    }

    @Test
    fun `collectVideosByRegion을 호출하면_해당 region의 키워드를 batchSize만큼 순차 처리한다`() {
        // given - 아시아 키워드가 존재하고, 검색 결과는 빈 상태
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(emptyList())

        // when - 아시아 region으로 2개씩 수집한다
        service.collectVideosByRegion("아시아", 2)

        // then - 정확히 2개의 키워드로 searchVideos가 호출된다
        verify(youTubePort, times(2)).searchVideos(any(), any())
    }

    @Test
    fun `collectVideosByRegion을 2번 연속 호출하면_이전 위치 다음부터 이어서 처리한다`() {
        // given - 검색 결과는 빈 상태
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(emptyList())

        // when - 아시아 region으로 2번 연속 수집한다
        service.collectVideosByRegion("아시아", 2)
        service.collectVideosByRegion("아시아", 2)

        // then - 총 4개의 키워드로 searchVideos가 호출된다 (2 + 2)
        verify(youTubePort, times(4)).searchVideos(any(), any())
    }

    @Test
    fun `존재하지 않는 region으로 collectVideosByRegion을 호출하면_아무 처리도 하지 않는다`() {
        // when - 존재하지 않는 region으로 수집한다
        service.collectVideosByRegion("남극")

        // then - searchVideos가 호출되지 않는다
        verify(youTubePort, never()).searchVideos(any(), any())
    }

    @Test
    fun `collectVideosByRegion에서 신규 영상을 발견하면_region 메타데이터를 태깅하여 저장한다`() {
        // given - 검색 결과와 신규 영상이 존재
        val searchResult =
            YouTubeSearchResult(
                videoId = "v-asia",
                title = "도쿄 여행",
                description = "desc",
                thumbnailUrl = "thumb",
                channelId = "ch1",
                channelTitle = "channel",
                publishedAt = "2024-01-01",
            )
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(listOf(searchResult))
        whenever(youTubeVideoPersistencePort.findExistingVideoIds(any())).thenReturn(emptySet())
        whenever(videoAnalyzeUseCase.analyzeVideo(any())).thenReturn(
            VideoAnalysisTask.create("https://www.youtube.com/watch?v=v-asia"),
        )

        val videoDetail =
            YouTubeVideoMeta(
                id = "id-1",
                videoId = "v-asia",
                title = "도쿄 여행",
                description = "desc",
                thumbnailUrl = "thumb",
                channelId = "ch1",
                channelTitle = "channel",
                viewCount = 500,
                likeCount = 50,
                duration = "PT5M",
                publishedAt = "2024-01-01",
                region = "",
                country = "",
                city = null,
                theme = null,
            )
        whenever(youTubePort.getVideoDetails(any())).thenReturn(listOf(videoDetail))

        // when - 아시아 region으로 1개씩 수집한다
        service.collectVideosByRegion("아시아", 1)

        // then - 저장이 수행된다
        verify(youTubeVideoPersistencePort).saveAll(any())
        verify(videoAnalyzeUseCase).analyzeVideo("https://www.youtube.com/watch?v=v-asia")
    }

    @Test
    fun `collectVideosByRegion에서 batchSize가 키워드 수보다 크면_전체 키워드를 순환하며 처리한다`() {
        // given - 검색 결과는 빈 상태
        whenever(youTubePort.searchVideos(any(), any())).thenReturn(emptyList())
        val asiaKeywordCount = SearchKeywordLoader.getByRegion("아시아").size

        // when - batchSize를 키워드 수 + 2로 설정하여 수집한다
        service.collectVideosByRegion("아시아", asiaKeywordCount + 2)

        // then - 키워드 수 + 2만큼 호출된다 (순환하여 처음부터 다시)
        verify(youTubePort, times(asiaKeywordCount + 2)).searchVideos(any(), any())
    }
}
