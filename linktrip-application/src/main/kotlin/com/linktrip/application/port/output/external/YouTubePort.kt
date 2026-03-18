package com.linktrip.application.port.output.external

import com.linktrip.application.domain.youtube.YouTubeChannelDetail
import com.linktrip.application.domain.youtube.YouTubeRecentVideo
import com.linktrip.application.domain.youtube.YouTubeSearchResult
import com.linktrip.application.domain.youtube.YouTubeVideoDetail

/**
 * YouTube Data API v3 호출을 위한 출력 포트.
 * 영상 검색, 영상 상세 조회, 채널 검색, 채널 상세 조회 기능을 제공한다.
 */
interface YouTubePort {
    /**
     * 키워드로 YouTube 영상을 검색한다. (search.list, type=video)
     * 여행 카테고리(videoCategoryId=19) 내에서 YouTube 알고리즘 관련성 순(order=relevance)으로 반환한다.
     * quota 비용: 100 units/회
     *
     * @param query 검색 키워드 (예: "도쿄 여행 vlog", "파리 맛집 여행")
     * @param maxResults 최대 반환 건수 (기본값 10, 최대 50)
     * @return 검색된 영상 목록 (videoId, 제목, 썸네일, 채널 정보 포함)
     */
    fun searchVideos(
        query: String,
        maxResults: Int = 10,
    ): List<YouTubeSearchResult>

    /**
     * 영상 ID 목록으로 영상의 상세 정보를 조회한다. (videos.list)
     * 조회수, 좋아요 수, 영상 길이 등 통계 정보를 포함하여 반환한다.
     * quota 비용: 1 unit/회
     *
     * @param videoIds 조회할 YouTube 영상 ID 목록 (최대 50개)
     * @return 영상 상세 정보 목록 (조회수, 좋아요, 영상 길이 포함)
     */
    fun getVideoDetails(videoIds: List<String>): List<YouTubeVideoDetail>

    /**
     * 키워드로 YouTube 채널을 검색한다. (search.list, type=channel → channels.list)
     * 검색 후 채널 ID로 상세 정보(구독자 수, 영상 수)를 추가 조회하여 반환한다.
     * quota 비용: 100 units(검색) + 1 unit(상세 조회) = 101 units/회
     *
     * @param query 검색 키워드 (예: "여행 vlog", "travel creator")
     * @param maxResults 최대 반환 건수 (기본값 10, 최대 50)
     * @param topicId YouTube 토픽 ID (예: "/m/07bxq" = Tourism). null이면 토픽 필터 미적용
     * @return 채널 상세 정보 목록 (구독자 수, 영상 수 포함)
     */
    fun searchChannels(
        query: String,
        maxResults: Int = 10,
        topicId: String? = null,
    ): List<YouTubeChannelDetail>

    /**
     * 채널 ID 목록으로 채널의 상세 정보를 조회한다. (channels.list)
     * 구독자 수, 총 영상 수, 프로필 이미지 등을 반환한다.
     * quota 비용: 1 unit/회
     *
     * @param channelIds 조회할 YouTube 채널 ID 목록 (최대 50개)
     * @return 채널 상세 정보 목록 (구독자 수, 영상 수 포함)
     */
    fun getChannelDetails(channelIds: List<String>): List<YouTubeChannelDetail>

    /**
     * 채널의 최신 업로드 영상을 조회한다. (playlistItems.list)
     * 채널 ID에서 업로드 플레이리스트 ID를 유도하여 (UC→UU) 최신 영상을 반환한다.
     * quota 비용: 1 unit/회
     *
     * @param channelId YouTube 채널 ID
     * @param maxResults 최대 반환 건수 (기본값 3)
     * @return 최신 영상 목록 (videoId, 제목, 썸네일, 게시일)
     */
    fun getRecentVideos(
        channelId: String,
        maxResults: Int = 3,
    ): List<YouTubeRecentVideo>
}
