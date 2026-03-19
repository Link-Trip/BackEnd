package com.linktrip.application.domain.youtube

import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DiscoverChannelServiceTest {
    @Mock
    lateinit var youTubeChannelPersistencePort: YouTubeChannelPersistencePort

    @InjectMocks
    lateinit var service: DiscoverChannelService

    @Test
    fun `저장된 채널이 5개이면_전체 5개를 모두 반환한다`() {
        // given - 5개의 채널이 저장되어 있는 상태
        val channels = (1..5).map { createChannel("ch$it") }
        whenever(youTubeChannelPersistencePort.findAll()).thenReturn(channels)

        // when - 채널 목록을 조회한다
        val result = service.getChannels()

        // then - 5개 전체를 반환한다
        assertEquals(5, result.size)
    }

    @Test
    fun `저장된 채널이 15개이면_랜덤으로 섞어서 최대 10개만 반환한다`() {
        // given - 15개의 채널이 저장되어 있는 상태
        val channels = (1..15).map { createChannel("ch$it") }
        whenever(youTubeChannelPersistencePort.findAll()).thenReturn(channels)

        // when - 채널 목록을 조회한다
        val result = service.getChannels()

        // then - 최대 10개만 반환한다
        assertEquals(10, result.size)
    }

    @Test
    fun `저장된 채널이 없으면_빈 리스트를 반환한다`() {
        // given - 저장된 채널이 없는 상태
        whenever(youTubeChannelPersistencePort.findAll()).thenReturn(emptyList())

        // when - 채널 목록을 조회한다
        val result = service.getChannels()

        // then - 빈 리스트를 반환한다
        assertTrue(result.isEmpty())
    }

    @Test
    fun `저장된 채널이 정확히 10개이면_전체를 반환한다 (경계값)`() {
        // given - 정확히 10개의 채널이 저장되어 있는 상태
        val channels = (1..10).map { createChannel("ch$it") }
        whenever(youTubeChannelPersistencePort.findAll()).thenReturn(channels)

        // when - 채널 목록을 조회한다
        val result = service.getChannels()

        // then - 10개 전체를 반환한다
        assertEquals(10, result.size)
    }

    private fun createChannel(channelId: String) =
        YouTubeChannelDetail(
            channelId = channelId,
            title = "Channel $channelId",
            description = "desc",
            thumbnailUrl = "thumb",
            subscriberCount = 100_000,
            videoCount = 50,
        )
}
