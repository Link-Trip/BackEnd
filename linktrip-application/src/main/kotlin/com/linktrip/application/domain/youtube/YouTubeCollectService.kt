package com.linktrip.application.domain.youtube

import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class YouTubeCollectService(
    private val youTubePort: YouTubePort,
    private val youTubeVideoPersistencePort: YouTubeVideoPersistencePort,
) {
    fun collectVideos() {
        val keywords = SearchKeyword.pickRandom()
        logger.info { "YouTube 영상 수집 시작 - 선택된 키워드: ${keywords.map { it.query }}" }

        val allVideos = mutableListOf<YouTubeVideoMeta>()

        keywords.forEach { keyword ->
            try {
                val searchResults = youTubePort.searchVideos(keyword.query, MAX_RESULTS_PER_KEYWORD)

                if (searchResults.isEmpty()) {
                    logger.info { "검색 결과 없음: ${keyword.query}" }
                    return@forEach
                }

                val videoIds = searchResults.map { it.videoId }
                val existingIds = youTubeVideoPersistencePort.findExistingVideoIds(videoIds)
                val newVideoIds = videoIds.filter { it !in existingIds }

                if (newVideoIds.isEmpty()) {
                    logger.info { "키워드 '${keyword.query}' - 신규 영상 없음 (${videoIds.size}건 모두 기존)" }
                    return@forEach
                }

                val videoDetails = youTubePort.getVideoDetails(newVideoIds)

                val taggedVideos =
                    videoDetails.map { detail ->
                        detail.copy(
                            region = keyword.region,
                            country = keyword.country,
                            city = keyword.city,
                            theme = keyword.theme,
                        )
                    }

                allVideos.addAll(taggedVideos)
                logger.info { "키워드 '${keyword.query}' 수집 완료: ${taggedVideos.size}건 신규 (${existingIds.size}건 기존 스킵)" }
            } catch (e: Exception) {
                logger.error(e) { "키워드 '${keyword.query}' 수집 실패" }
            }
        }

        if (allVideos.isNotEmpty()) {
            youTubeVideoPersistencePort.saveAll(allVideos)
            logger.info { "YouTube 영상 수집 완료 - 총 ${allVideos.size}건 저장" }
        }
    }

    companion object {
        private const val MAX_RESULTS_PER_KEYWORD = 10
    }
}
