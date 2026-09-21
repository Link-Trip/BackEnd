package com.linktrip.output.persistence.mysql.repository

import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.output.persistence.mysql.entity.QVideoAnalysisTaskEntity
import com.linktrip.output.persistence.mysql.entity.QYouTubeVideoEntity
import com.linktrip.output.persistence.mysql.entity.YouTubeVideoEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class YouTubeVideoQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val video = QYouTubeVideoEntity.youTubeVideoEntity
    private val analysisTask = QVideoAnalysisTaskEntity.videoAnalysisTaskEntity

    fun findAllByVideoIdIn(videoIds: List<String>): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .where(video.videoId.`in`(videoIds))
            .fetch()

    fun findVideoIdsByVideoIdIn(videoIds: List<String>): List<String> =
        queryFactory
            .select(video.videoId)
            .from(video)
            .where(video.videoId.`in`(videoIds))
            .fetch()

    fun findAllOrderByViewCountDesc(): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .orderBy(video.viewCount.desc())
            .fetch()

    fun findAllByCountryOrderByViewCountDesc(country: String): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .where(video.country.eq(country))
            .orderBy(video.viewCount.desc())
            .fetch()

    fun findAllByRegionOrderByViewCountDesc(region: String): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .where(video.region.eq(region))
            .orderBy(video.viewCount.desc())
            .fetch()

    fun findAllByThemeAndCreatedAtBefore(
        theme: String,
        cursor: LocalDateTime,
        pageable: Pageable,
    ): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .where(
                video.theme.eq(theme),
                video.createdAt.before(cursor),
            )
            .orderBy(video.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

    fun findAllByTheme(
        theme: String,
        pageable: Pageable,
    ): List<YouTubeVideoEntity> =
        queryFactory
            .selectFrom(video)
            .where(video.theme.eq(theme))
            .orderBy(video.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

    /**
     * youtube_video 에 있지만 video_analysis_task 에는 없는 videoId 를 오래된 순으로 [limit] 건 조회.
     *
     * LEFT JOIN 후 매칭된 분석 task 가 없는(IS NULL) row 만 선택. SQL 의 anti-join 정석 패턴.
     *
     * ```sql
     * SELECT v.video_id
     * FROM youtube_video v
     * LEFT JOIN video_analysis_task t
     *   ON t.youtube_url = CONCAT('https://www.youtube.com/watch?v=', v.video_id)
     * WHERE t.id IS NULL
     * ORDER BY v.created_at ASC
     * LIMIT :limit
     * ```
     */
    fun findUnanalyzedVideoIds(limit: Int): List<String> {
        val constructedUrl = video.videoId.prepend(VideoAnalysisTask.YOUTUBE_VIDEO_BASE_URL)
        return queryFactory
            .select(video.videoId)
            .from(video)
            .leftJoin(analysisTask).on(analysisTask.youtubeUrl.eq(constructedUrl))
            .where(analysisTask.id.isNull)
            .orderBy(video.createdAt.asc())
            .limit(limit.toLong())
            .fetch()
    }
}
