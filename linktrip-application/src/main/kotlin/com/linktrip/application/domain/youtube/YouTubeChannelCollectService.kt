package com.linktrip.application.domain.youtube

import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.application.port.output.persistence.YouTubeChannelPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class YouTubeChannelCollectService(
    private val youTubePort: YouTubePort,
    private val youTubeChannelPersistencePort: YouTubeChannelPersistencePort,
) {
    fun collectChannels() {
        logger.info { "YouTube 채널 수집 시작" }

        val allChannels = mutableListOf<YouTubeChannelDetail>()
        var failedKeywords = 0

        CHANNEL_SEARCH_KEYWORDS.forEach { keyword ->
            try {
                val channels =
                    youTubePort.searchChannels(
                        query = keyword,
                        maxResults = MAX_RESULTS_PER_KEYWORD,
                        topicId = TOPIC_TOURISM,
                    )

                if (channels.isEmpty()) {
                    logger.info { "채널 검색 결과 없음: $keyword" }
                    return@forEach
                }

                val filtered =
                    channels.filter { channel ->
                        channel.subscriberCount >= MIN_SUBSCRIBER_COUNT &&
                            EXCLUDED_KEYWORDS.none { excluded ->
                                channel.title.contains(excluded, ignoreCase = true)
                            }
                    }

                allChannels.addAll(filtered)
                val skipped = channels.size - filtered.size
                logger.info {
                    "키워드 '$keyword' 채널 수집: ${filtered.size}건 (${skipped}건 필터링)"
                }
            } catch (e: Exception) {
                failedKeywords++
                logger.error(e) { "키워드 '$keyword' 채널 수집 실패" }
            }
        }

        if (allChannels.isEmpty()) {
            if (failedKeywords == CHANNEL_SEARCH_KEYWORDS.size) {
                throw IllegalStateException("YouTube 채널 수집 실패: 모든 키워드 조회 실패")
            }
            logger.info { "YouTube 채널 수집 완료 - 신규 채널 없음" }
            return
        }

        val dedupedChannels =
            allChannels.associateBy { it.channelId }.values
                .take(MAX_TOTAL_CHANNELS)

        val channelsWithVideos =
            dedupedChannels.map { channel ->
                try {
                    val recentVideos = youTubePort.getRecentVideos(channel.channelId)
                    channel.copy(recentVideos = recentVideos)
                } catch (e: Exception) {
                    logger.warn(e) { "채널 '${channel.title}' 최신 영상 조회 실패" }
                    channel
                }
            }

        youTubeChannelPersistencePort.saveAll(channelsWithVideos)
        logger.info { "YouTube 채널 수집 완료 - 총 ${channelsWithVideos.size}건 저장" }
    }

    companion object {
        private const val TOPIC_TOURISM = "/m/07bxq"
        private const val MIN_SUBSCRIBER_COUNT = 100_000L
        private const val MAX_RESULTS_PER_KEYWORD = 50
        private const val MAX_TOTAL_CHANNELS = 50

        private val EXCLUDED_KEYWORDS =
            listOf(
                "EBS", "KBS", "MBC", "SBS", "JTBC",
                "tvN", "YTN", "채널A", "TV조선", "MBN",
            )

        private val CHANNEL_SEARCH_KEYWORDS =
            listOf(
                "여행 브이로그",
                "여행 유튜버",
                "세계여행 브이로그",
                "해외여행 유튜버",
                "여행 크리에이터",
                "배낭여행 브이로그",
            )
    }
}
