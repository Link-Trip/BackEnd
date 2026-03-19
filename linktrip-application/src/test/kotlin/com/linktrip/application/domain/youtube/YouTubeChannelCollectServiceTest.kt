package com.linktrip.application.domain.youtube

import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class YouTubeChannelCollectServiceTest {
    @Mock
    lateinit var youTubePort: YouTubePort

    @Mock
    lateinit var youTubeChannelPersistencePort: YouTubeChannelPersistencePort

    @InjectMocks
    lateinit var service: YouTubeChannelCollectService

    @Test
    fun `구독자 20만 채널과 5만 채널이 있으면_10만 이상인 20만 채널만 저장된다`() {
        // given - 구독자 20만(기준 이상)과 5만(기준 미만) 채널
        val channels = listOf(
            createChannel("ch1", "여행 유튜버A", 200_000),
            createChannel("ch2", "소규모 채널", 50_000),
        )
        whenever(youTubePort.searchChannels(any(), any(), any())).thenReturn(channels)
        whenever(youTubePort.getRecentVideos(any(), any())).thenReturn(emptyList())

        // when - 채널 수집을 실행한다
        service.collectChannels()

        // then - 구독자 10만 이상인 ch1만 저장된다
        val captor = argumentCaptor<List<YouTubeChannelDetail>>()
        verify(youTubeChannelPersistencePort).saveAll(captor.capture())

        val saved = captor.firstValue
        //   빈 리스트가 아닌, 정확히 1건이 저장되어야 한다 (vacuous truth 방지)
        assertEquals(1, saved.size) { "구독자 10만 이상인 채널만 저장되어야 한다" }
        assertEquals("ch1", saved[0].channelId)
        assertTrue(saved[0].subscriberCount >= 100_000)
    }

    @Test
    fun `방송사 채널명이 대소문자 혼합이어도_필터링되어 일반 채널만 저장된다`() {
        // given - 대소문자 혼합된 방송사 채널 + 일반 채널
        //   프로덕션 코드의 contains(excluded, ignoreCase = true)를 실제로 검증한다
        val channels = listOf(
            createChannel("ch1", "kbs 여행 프로그램", 500_000),   // 소문자 kbs
            createChannel("ch2", "Mbc 다큐멘터리", 300_000),     // 혼합 Mbc
            createChannel("ch3", "JTBC 여행기", 200_000),        // 대문자 JTBC
            createChannel("ch4", "일반 여행 유튜버", 200_000),    // 일반 채널
        )
        whenever(youTubePort.searchChannels(any(), any(), any())).thenReturn(channels)
        whenever(youTubePort.getRecentVideos(any(), any())).thenReturn(emptyList())

        // when - 채널 수집을 실행한다
        service.collectChannels()

        // then - 대소문자 무관하게 방송사 채널은 전부 필터링, 일반 채널만 저장
        val captor = argumentCaptor<List<YouTubeChannelDetail>>()
        verify(youTubeChannelPersistencePort).saveAll(captor.capture())

        val saved = captor.firstValue
        assertEquals(1, saved.size) { "방송사 채널 3개가 전부 필터링되고 일반 채널 1개만 남아야 한다" }
        assertEquals("ch4", saved[0].channelId)
        assertEquals("일반 여행 유튜버", saved[0].title)
    }

    @Test
    fun `6개 검색 키워드가 모두 API 오류로 실패하면_IllegalStateException이 발생한다`() {
        // given - 모든 키워드 검색에서 API 오류가 발생하는 상태
        whenever(youTubePort.searchChannels(any(), any(), any())).thenThrow(RuntimeException("API 오류"))

        // when & then - 모든 키워드가 실패했으므로 IllegalStateException이 발생한다
        //   프로덕션 코드: failedKeywords == CHANNEL_SEARCH_KEYWORDS.size 조건 검증
        val exception = assertThrows<IllegalStateException> {
            service.collectChannels()
        }
        assertTrue(exception.message!!.contains("모든 키워드")) {
            "예외 메시지에 '모든 키워드'가 포함되어야 한다"
        }
    }

    @Test
    fun `최신 영상 목록 조회에 실패한 채널은_recentVideos가 비어있는 상태로 저장된다`() {
        // given - 채널 1개가 있고, getRecentVideos 호출 시 예외 발생
        val channels = listOf(createChannel("ch1", "여행 유튜버", 200_000))
        whenever(youTubePort.searchChannels(any(), any(), any())).thenReturn(channels)
        whenever(youTubePort.getRecentVideos(eq("ch1"), any())).thenThrow(RuntimeException("영상 조회 실패"))

        // when - 채널 수집을 실행한다
        service.collectChannels()

        // then - 채널 자체는 저장되되, recentVideos는 비어있다
        //   프로덕션 코드: catch 블록에서 원본 channel(recentVideos 기본값 = emptyList)을 그대로 반환
        val captor = argumentCaptor<List<YouTubeChannelDetail>>()
        verify(youTubeChannelPersistencePort).saveAll(captor.capture())

        val saved = captor.firstValue
        assertEquals(1, saved.size)
        assertEquals("ch1", saved[0].channelId)
        assertEquals(emptyList<YouTubeRecentVideo>(), saved[0].recentVideos) {
            "getRecentVideos 실패 시 원본 channel의 빈 recentVideos가 유지되어야 한다"
        }
    }

    @Test
    fun `여러 키워드에서 동일 채널이 중복 검색되면_channelId 기준으로 중복 제거하여 1건만 저장한다`() {
        // given - 6개 키워드 모두 동일한 channelId "ch1" 채널을 반환
        //   searchChannels는 키워드마다 호출되므로 6번 호출 × 2건 = 총 12건이 allChannels에 추가된다
        val channels = listOf(
            createChannel("ch1", "여행 유튜버A", 200_000),
            createChannel("ch1", "여행 유튜버A", 200_000),
        )
        whenever(youTubePort.searchChannels(any(), any(), any())).thenReturn(channels)
        whenever(youTubePort.getRecentVideos(any(), any())).thenReturn(emptyList())

        // when - 채널 수집을 실행한다
        service.collectChannels()

        // then - associateBy { channelId }로 중복 제거되어 정확히 1건만 저장
        val captor = argumentCaptor<List<YouTubeChannelDetail>>()
        verify(youTubeChannelPersistencePort).saveAll(captor.capture())

        val saved = captor.firstValue
        assertEquals(1, saved.size) { "12건의 중복 채널이 channelId 기준으로 1건만 남아야 한다" }
        assertEquals("ch1", saved[0].channelId)
    }

    @Test
    fun `모든 키워드에서 검색 결과가 없으면_saveAll을 호출하지 않는다`() {
        // given - 모든 키워드의 검색 결과가 비어있는 상태
        whenever(youTubePort.searchChannels(any(), any(), any())).thenReturn(emptyList())

        // when - 채널 수집을 실행한다
        service.collectChannels()

        // then - allChannels가 비어있고 failedKeywords도 0이므로
        //   IllegalStateException 없이 return하며, saveAll은 호출되지 않는다
        verify(youTubeChannelPersistencePort, never()).saveAll(any())
    }

    private fun createChannel(channelId: String, title: String, subscriberCount: Long) = YouTubeChannelDetail(
        channelId = channelId,
        title = title,
        description = "desc",
        thumbnailUrl = "thumb",
        subscriberCount = subscriberCount,
        videoCount = 50,
    )
}
