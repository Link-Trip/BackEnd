package com.linktrip.output.http.dto.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTubeSearchResponse(
    val items: List<SearchItem>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchItem(
        val id: SearchItemId,
        val snippet: Snippet?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchItemId(
        val videoId: String?,
        val channelId: String?,
    )
}
