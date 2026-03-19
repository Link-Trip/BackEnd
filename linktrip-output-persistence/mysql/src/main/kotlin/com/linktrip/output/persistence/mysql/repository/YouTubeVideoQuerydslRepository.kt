package com.linktrip.output.persistence.mysql.repository

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
}
