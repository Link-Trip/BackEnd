package com.linktrip.application.domain.youtube

import com.linktrip.application.domain.video.Source
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.external.YouTubePort
import com.linktrip.application.port.output.persistence.YouTubeVideoPersistencePort
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class YouTubeCollectService(
    private val youTubePort: YouTubePort,
    private val youTubeVideoPersistencePort: YouTubeVideoPersistencePort,
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
) {
    private val regionIndexMap = mutableMapOf<String, Int>()

    fun collectVideos() {
        val keywords = SearchKeywordLoader.pickRandom()
        collectByKeywords(keywords)
    }

    fun collectVideosByRegion(
        region: String,
        batchSize: Int = DEFAULT_BATCH_SIZE,
    ) {
        val keywords = SearchKeywordLoader.getByRegion(region)
        if (keywords.isEmpty()) {
            logger.warn { "해당 region의 키워드가 없습니다: $region" }
            return
        }

        val currentIndex = regionIndexMap.getOrDefault(region, 0)
        val selectedKeywords = pickNextBatch(keywords, currentIndex, batchSize)

        val nextIndex = (currentIndex + batchSize) % keywords.size
        regionIndexMap[region] = nextIndex

        logger.info {
            "YouTube 영상 수집 시작 [$region] - 인덱스 $currentIndex → $nextIndex, " +
                "키워드: ${selectedKeywords.map { it.query }}"
        }
        collectByKeywords(selectedKeywords)
    }

    private fun collectByKeywords(keywords: List<SearchKeyword>) {
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
            requestVideoAnalysis(allVideos)
            logger.info { "YouTube 영상 수집 완료 - 총 ${allVideos.size}건 저장" }
        }
    }

    private fun requestVideoAnalysis(videos: List<YouTubeVideoMeta>) {
        videos.forEach { video ->
            try {
                videoAnalyzeUseCase.analyzeVideo(
                    youtubeUrl = VideoAnalysisTask.buildUrl(video.videoId),
                    source = Source.BATCH,
                )
            } catch (e: Exception) {
                logger.warn(e) { "영상 분석 요청 실패: videoId=${video.videoId}" }
            }
        }
    }

    private fun pickNextBatch(
        keywords: List<SearchKeyword>,
        startIndex: Int,
        batchSize: Int,
    ): List<SearchKeyword> {
        val result = mutableListOf<SearchKeyword>()
        for (i in 0 until batchSize) {
            result.add(keywords[(startIndex + i) % keywords.size])
        }
        return result
    }

    companion object {
        private const val MAX_RESULTS_PER_KEYWORD = 10
        private const val DEFAULT_BATCH_SIZE = 4
    }
}
