package com.linktrip.output.http.dto.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTubeVideoResponse(
    val items: List<VideoItem>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VideoItem(
        val id: String,
        val snippet: Snippet?,
        val statistics: Statistics?,
        val contentDetails: ContentDetails?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Statistics(
        val viewCount: String?,
        val likeCount: String?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ContentDetails(
        val duration: String?,
    )
}
