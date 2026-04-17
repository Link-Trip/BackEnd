package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.domain.youtube.YouTubeVideoMeta
import java.time.LocalDateTime

interface YouTubeVideoPersistencePort {
    fun saveAll(videos: List<YouTubeVideoMeta>)

    fun findExistingVideoIds(videoIds: List<String>): Set<String>

    fun findAll(): List<YouTubeVideoMeta>

    fun findAllByCountry(country: String): List<YouTubeVideoMeta>

    fun findAllByRegion(region: String): List<YouTubeVideoMeta>

    fun findAllByTheme(
        theme: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<YouTubeVideoMeta>

    /**
     * youtube_video 에 있지만 아직 video_analysis_task 가 생성되지 않은 (= 한 번도 분석 요청된 적 없는) videoId 목록.
     * 오래된 것부터 [limit] 건. backfill  스케줄러에서 stranded 영상 소진용.
     */
    fun findUnanalyzedVideoIds(limit: Int): List<String>
}
