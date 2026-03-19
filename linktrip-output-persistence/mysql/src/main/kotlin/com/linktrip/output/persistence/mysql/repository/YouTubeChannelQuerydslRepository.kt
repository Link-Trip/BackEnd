package com.linktrip.output.persistence.mysql.repository

import com.linktrip.output.persistence.mysql.entity.QYouTubeChannelEntity
import com.linktrip.output.persistence.mysql.entity.YouTubeChannelEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class YouTubeChannelQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val channel = QYouTubeChannelEntity.youTubeChannelEntity

    fun findAllByChannelIdIn(channelIds: List<String>): List<YouTubeChannelEntity> =
        queryFactory
            .selectFrom(channel)
            .where(channel.channelId.`in`(channelIds))
            .fetch()

    fun findAllOrderBySubscriberCountDesc(): List<YouTubeChannelEntity> =
        queryFactory
            .selectFrom(channel)
            .orderBy(channel.subscriberCount.desc())
            .fetch()
}
