package com.linktrip.application.domain.youtube

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object SearchKeywordLoader {
    private val keywords: List<SearchKeyword> by lazy { load() }

    fun getAll(): List<SearchKeyword> = keywords

    fun getByRegion(region: String): List<SearchKeyword> = keywords.filter { it.region == region }

    fun pickRandom(count: Int = RANDOM_PICK_COUNT): List<SearchKeyword> = keywords.shuffled().take(count)

    private fun load(): List<SearchKeyword> {
        val stream =
            SearchKeywordLoader::class.java.classLoader
                .getResourceAsStream(RESOURCE_PATH)
                ?: throw IllegalStateException("검색 키워드 파일을 찾을 수 없습니다: $RESOURCE_PATH")

        val loaded = stream.use { jacksonObjectMapper().readValue<List<SearchKeyword>>(it) }
        logger.info { "검색 키워드 로드 완료: ${loaded.size}개" }
        return loaded
    }

    private const val RESOURCE_PATH = "data/search-keywords.json"
    private const val RANDOM_PICK_COUNT = 5
}
