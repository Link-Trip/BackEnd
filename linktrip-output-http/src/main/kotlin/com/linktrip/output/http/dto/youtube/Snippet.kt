package com.linktrip.output.http.dto.youtube

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Snippet(
    val title: String?,
    val description: String?,
    val thumbnails: Thumbnails?,
    val channelId: String?,
    val channelTitle: String?,
    val publishedAt: String?,
    val categoryId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Thumbnails(
    val high: Thumbnail?,
    val medium: Thumbnail?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Thumbnail(
    val url: String?,
)
