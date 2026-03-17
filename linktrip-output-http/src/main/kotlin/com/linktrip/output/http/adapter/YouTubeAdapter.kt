package com.linktrip.output.http.adapter

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.domain.youtube.YouTubeVideoDetail
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.output.http.dto.youtube.YouTubeChannelResponse
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

    override fun getVideoDetails(videoIds: List<String>): List<YouTubeVideoDetail> {
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
                YouTubeVideoDetail(
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
                )
            } ?: emptyList()
        }
    }

    override fun searchChannels(
        query: String,
        maxResults: Int,
    ): List<YouTubeChannelDetail> {
        val searchResponse =
            youtubeRestClient.get()
                .uri { builder ->
                    builder
                        .path(SEARCH_URI)
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "channel")
                        .queryParam("order", "relevance")
                        .queryParam("maxResults", maxResults)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
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

    companion object {
        private const val SEARCH_URI = "/search"
        private const val VIDEOS_URI = "/videos"
        private const val CHANNELS_URI = "/channels"
        private const val TRAVEL_CATEGORY_ID = "19"
        private const val MAX_IDS_PER_REQUEST = 50
    }
}
