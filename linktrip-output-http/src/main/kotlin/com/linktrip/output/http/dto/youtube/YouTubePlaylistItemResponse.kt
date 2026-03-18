package com.linktrip.output.http.dto.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTubePlaylistItemResponse(
    val items: List<PlaylistItem>?,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlaylistItem(
        val snippet: PlaylistSnippet?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlaylistSnippet(
        val title: String?,
        val thumbnails: Thumbnails?,
        val publishedAt: String?,
        val resourceId: ResourceId?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ResourceId(
        val videoId: String?,
    )
}
