package com.linktrip.bootstrap

import com.linktrip.application.domain.youtube.SearchKeywordLoader
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ApplicationInitializer {
    @PostConstruct
    fun init() {
        loadSearchKeywords()
    }

    private fun loadSearchKeywords() {
        val startTime = System.currentTimeMillis()
        val keywords = SearchKeywordLoader.getAll()
        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "검색 키워드 초기화 완료: ${keywords.size}개, ${elapsed}ms" }
    }
}
