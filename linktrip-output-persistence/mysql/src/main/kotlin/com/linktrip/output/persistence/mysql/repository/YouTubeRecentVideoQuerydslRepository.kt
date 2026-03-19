package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QYouTubeRecentVideoEntity
import com.linktrip.output.persistence.mysql.entity.YouTubeRecentVideoEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class YouTubeRecentVideoQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val recentVideo = QYouTubeRecentVideoEntity.youTubeRecentVideoEntity

    fun findAllByChannelIdIn(channelIds: List<String>): List<YouTubeRecentVideoEntity> =
        queryFactory
            .selectFrom(recentVideo)
            .where(recentVideo.channelId.`in`(channelIds))
            .fetch()

    fun deleteAllByChannelIdIn(channelIds: List<String>): Long =
        queryFactory
            .delete(recentVideo)
            .where(recentVideo.channelId.`in`(channelIds))
            .execute()
}
