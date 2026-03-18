package com.linktrip.application.port.input

import com.linktrip.application.domain.youtube.YouTubeChannelDetail

interface DiscoverChannelUseCase {
    fun getChannels(): List<YouTubeChannelDetail>
}
