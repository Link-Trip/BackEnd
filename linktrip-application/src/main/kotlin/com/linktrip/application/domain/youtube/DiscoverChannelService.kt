package com.linktrip.application.domain.youtube

import com.linktrip.application.port.input.DiscoverChannelUseCase
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import org.springframework.stereotype.Service

@Service
class DiscoverChannelService(
    private val youTubeChannelPersistencePort: YouTubeChannelPersistencePort,
) : DiscoverChannelUseCase {
    override fun getChannels(): List<YouTubeChannelDetail> {
        val allChannels = youTubeChannelPersistencePort.findAll()
        if (allChannels.size <= RESPONSE_CHANNEL_LIMIT) return allChannels
        return allChannels.shuffled().take(RESPONSE_CHANNEL_LIMIT)
    }

    companion object {
        private const val RESPONSE_CHANNEL_LIMIT = 10
    }
}
