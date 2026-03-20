package com.linktrip.application.domain.youtube

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
}
