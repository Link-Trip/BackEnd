package com.linktrip.output.http.dto.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTubeChannelResponse(
    val items: List<ChannelItem>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChannelItem(
        val id: String,
        val snippet: Snippet?,
        val statistics: ChannelStatistics?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChannelStatistics(
        val subscriberCount: String?,
        val videoCount: String?,
    )
}
