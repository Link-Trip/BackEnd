package com.linktrip.output.http.adapter

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeRecentVideo
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.output.http.dto.youtube.YouTubeChannelResponse
import com.linktrip.output.http.dto.youtube.YouTubePlaylistItemResponse
import com.linktrip.output.http.dto.youtube.YouTubeSearchResponse
import com.linktrip.output.http.dto.youtube.YouTubeVideoResponse
import com.linktrip.output.http.properties.YouTubeProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class YouTubeAdapter(
    private val youtubeProperties: YouTubeProperties,
    private val youtubeRestClient: RestClient,
) : YouTubePort {
    override fun searchVideos(
        query: String,
        maxResults: Int,
    ): List<YouTubeSearchResult> {
        val response =
            youtubeRestClient.get()
                .uri { builder ->
                    builder
                        .path(SEARCH_URI)
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "video")
                        .queryParam("order", "relevance")
                        .queryParam("videoCategoryId", TRAVEL_CATEGORY_ID)
                        .queryParam("maxResults", maxResults)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body<YouTubeSearchResponse>()

        return response?.items
            ?.filter { it.id.videoId != null }
            ?.map { item ->
                YouTubeSearchResult(
                    videoId = item.id.videoId!!,
                    title = item.snippet?.title.orEmpty(),
                    description = item.snippet?.description.orEmpty(),
                    thumbnailUrl =
                        item.snippet?.thumbnails?.high?.url
                            ?: item.snippet?.thumbnails?.medium?.url.orEmpty(),
                    channelId = item.snippet?.channelId.orEmpty(),
                    channelTitle = item.snippet?.channelTitle.orEmpty(),
                    publishedAt = item.snippet?.publishedAt.orEmpty(),
                )
            } ?: emptyList()
    }

    override fun getVideoDetails(videoIds: List<String>): List<YouTubeVideoMeta> {
        if (videoIds.isEmpty()) return emptyList()

        return videoIds.chunked(MAX_IDS_PER_REQUEST).flatMap { chunk ->
            val response =
                youtubeRestClient.get()
                    .uri { builder ->
                        builder
                            .path(VIDEOS_URI)
                            .queryParam("part", "snippet,statistics,contentDetails")
                            .queryParam("id", chunk.joinToString(","))
                            .queryParam("key", youtubeProperties.apiKey)
                            .build()
                    }
                    .retrieve()
                    .body<YouTubeVideoResponse>()

            response?.items?.map { item ->
                YouTubeVideoMeta.create(
                    videoId = item.id,
                    title = item.snippet?.title.orEmpty(),
                    description = item.snippet?.description.orEmpty(),
                    thumbnailUrl =
                        item.snippet?.thumbnails?.high?.url
                            ?: item.snippet?.thumbnails?.medium?.url.orEmpty(),
                    channelId = item.snippet?.channelId.orEmpty(),
                    channelTitle = item.snippet?.channelTitle.orEmpty(),
                    viewCount = item.statistics?.viewCount?.toLongOrNull() ?: 0L,
                    likeCount = item.statistics?.likeCount?.toLongOrNull() ?: 0L,
                    duration = item.contentDetails?.duration.orEmpty(),
                    publishedAt = item.snippet?.publishedAt.orEmpty(),
                    region = "",
                    country = "",
                    city = null,
                    theme = null,
                )
            } ?: emptyList()
        }
    }

    override fun searchChannels(
        query: String,
        maxResults: Int,
        topicId: String?,
    ): List<YouTubeChannelDetail> {
        val searchResponse =
            youtubeRestClient.get()
                .uri { builder ->
                    builder
                        .path(SEARCH_URI)
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "channel")
                        .queryParam("order", "viewCount")
                        .queryParam("maxResults", maxResults)
                        .queryParam("relevanceLanguage", "ko")
                        .queryParam("key", youtubeProperties.apiKey)
                    if (topicId != null) {
                        builder.queryParam("topicId", topicId)
                    }
                    builder.build()
                }
                .retrieve()
                .body<YouTubeSearchResponse>()

        val channelIds =
            searchResponse?.items
                ?.mapNotNull { it.id.channelId }
                ?: return emptyList()

        return getChannelDetails(channelIds)
    }

    override fun getChannelDetails(channelIds: List<String>): List<YouTubeChannelDetail> {
        if (channelIds.isEmpty()) return emptyList()

        return channelIds.chunked(MAX_IDS_PER_REQUEST).flatMap { chunk ->
            val response =
                youtubeRestClient.get()
                    .uri { builder ->
                        builder
                            .path(CHANNELS_URI)
                            .queryParam("part", "snippet,statistics")
                            .queryParam("id", chunk.joinToString(","))
                            .queryParam("key", youtubeProperties.apiKey)
                            .build()
                    }
                    .retrieve()
                    .body<YouTubeChannelResponse>()

            response?.items?.map { item ->
                YouTubeChannelDetail(
                    channelId = item.id,
                    title = item.snippet?.title.orEmpty(),
                    description = item.snippet?.description.orEmpty(),
                    thumbnailUrl =
                        item.snippet?.thumbnails?.high?.url
                            ?: item.snippet?.thumbnails?.medium?.url.orEmpty(),
                    subscriberCount = item.statistics?.subscriberCount?.toLongOrNull() ?: 0L,
                    videoCount = item.statistics?.videoCount?.toLongOrNull() ?: 0L,
                )
            } ?: emptyList()
        }
    }

    override fun getRecentVideos(
        channelId: String,
        maxResults: Int,
    ): List<YouTubeRecentVideo> {
        val uploadsPlaylistId = channelId.replaceFirst("UC", "UU")

        val response =
            youtubeRestClient.get()
                .uri { builder ->
                    builder
                        .path(PLAYLIST_ITEMS_URI)
                        .queryParam("part", "snippet")
                        .queryParam("playlistId", uploadsPlaylistId)
                        .queryParam("maxResults", FETCH_VIDEOS_FOR_FILTERING)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body<YouTubePlaylistItemResponse>()

        val candidates =
            response?.items?.mapNotNull { item ->
                val snippet = item.snippet ?: return@mapNotNull null
                val videoId = snippet.resourceId?.videoId ?: return@mapNotNull null
                YouTubeRecentVideo.create(
                    channelId = channelId,
                    videoId = videoId,
                    title = snippet.title.orEmpty(),
                    thumbnailUrl =
                        snippet.thumbnails?.high?.url
                            ?: snippet.thumbnails?.medium?.url.orEmpty(),
                    publishedAt = snippet.publishedAt.orEmpty(),
                )
            } ?: emptyList()

        if (candidates.isEmpty()) return emptyList()

        val categoryMap = getVideoCategoryIds(candidates.map { it.videoId })

        val travelVideos = candidates.filter { categoryMap[it.videoId] == TRAVEL_CATEGORY_ID }
        if (travelVideos.size >= maxResults) {
            return travelVideos.take(maxResults)
        }

        val nonTravelVideos = candidates.filter { categoryMap[it.videoId] != TRAVEL_CATEGORY_ID }
        return (travelVideos + nonTravelVideos).take(maxResults)
    }

    private fun getVideoCategoryIds(videoIds: List<String>): Map<String, String> {
        if (videoIds.isEmpty()) return emptyMap()

        return videoIds.chunked(MAX_IDS_PER_REQUEST).flatMap { chunk ->
            val response =
                youtubeRestClient.get()
                    .uri { builder ->
                        builder
                            .path(VIDEOS_URI)
                            .queryParam("part", "snippet")
                            .queryParam("id", chunk.joinToString(","))
                            .queryParam("key", youtubeProperties.apiKey)
                            .build()
                    }
                    .retrieve()
                    .body<YouTubeVideoResponse>()

            response?.items?.mapNotNull { item ->
                val categoryId = item.snippet?.categoryId ?: return@mapNotNull null
                item.id to categoryId
            } ?: emptyList()
        }.toMap()
    }

    companion object {
        private const val SEARCH_URI = "/search"
        private const val VIDEOS_URI = "/videos"
        private const val CHANNELS_URI = "/channels"
        private const val PLAYLIST_ITEMS_URI = "/playlistItems"
        private const val TRAVEL_CATEGORY_ID = "19"
        private const val MAX_IDS_PER_REQUEST = 50
        private const val FETCH_VIDEOS_FOR_FILTERING = 10
    }
}
