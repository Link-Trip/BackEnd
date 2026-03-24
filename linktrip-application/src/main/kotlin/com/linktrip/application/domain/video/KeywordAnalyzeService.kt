package com.linktrip.application.domain.video

import com.linktrip.application.domain.youtube.SearchKeyword
import com.linktrip.application.domain.youtube.SearchKeywordLoader
import com.linktrip.application.port.input.KeywordAnalyzeResult
import com.linktrip.application.port.input.KeywordAnalyzeUseCase
import com.linktrip.application.port.input.VideoAnalyzeUseCase
import com.linktrip.application.port.output.external.YouTubePort
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class KeywordAnalyzeService(
    private val youTubePort: YouTubePort,
    private val videoAnalyzeUseCase: VideoAnalyzeUseCase,
) : KeywordAnalyzeUseCase {
    override fun analyzeByKeywords(
        region: String?,
        country: String?,
        maxResults: Int,
    ): KeywordAnalyzeResult {
        val keywords = filterKeywords(region, country)

        if (keywords.isEmpty()) {
            return KeywordAnalyzeResult(keywordCount = 0, tasks = emptyList())
        }

        logger.info { "키워드 기반 분석 시작: keywords=${keywords.size}, region=$region, country=$country" }

        val tasks =
            keywords.flatMap { keyword ->
                searchAndAnalyze(keyword, maxResults)
            }

        logger.info { "키워드 기반 분석 완료: keywords=${keywords.size}, analyzed=${tasks.size}" }

        return KeywordAnalyzeResult(keywordCount = keywords.size, tasks = tasks)
    }

    private fun filterKeywords(
        region: String?,
        country: String?,
    ): List<SearchKeyword> =
        SearchKeywordLoader.getAll().filter { keyword ->
            (region.isNullOrBlank() || keyword.region == region) &&
                (country.isNullOrBlank() || keyword.country == country)
        }

    private fun searchAndAnalyze(
        keyword: SearchKeyword,
        maxResults: Int,
    ): List<VideoAnalysisTask> =
        try {
            val searchResults = youTubePort.searchVideos(keyword.query, maxResults)
            searchResults.mapNotNull { video ->
                analyzeVideo(video.videoId)
            }
        } catch (e: Exception) {
            logger.warn(e) { "키워드 검색 실패: ${keyword.query}" }
            emptyList()
        }

    private fun analyzeVideo(videoId: String): VideoAnalysisTask? =
        try {
            videoAnalyzeUseCase.analyzeVideo(VideoAnalysisTask.buildUrl(videoId))
        } catch (e: Exception) {
            logger.warn { "영상 분석 요청 스킵: videoId=$videoId, reason=${e.message}" }
            null
        }
}
