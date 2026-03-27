package com.linktrip.bootstrap

import com.linktrip.application.domain.youtube.SearchKeywordLoader
import com.linktrip.output.http.properties.GcpProperties
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class ApplicationInitializer(
    private val gcpProperties: GcpProperties,
) {
    @PostConstruct
    fun init() {
        validateGcpCredentialsFile()
        loadSearchKeywords()
    }

    private fun validateGcpCredentialsFile() {
        val file = File(gcpProperties.credentialsPath)
        require(file.exists()) {
            "GCP credentials 파일이 존재하지 않습니다: ${gcpProperties.credentialsPath}"
        }
        logger.info { "GCP credentials 파일 확인 완료: ${gcpProperties.credentialsPath}" }
    }

    private fun loadSearchKeywords() {
        val startTime = System.currentTimeMillis()
        val keywords = SearchKeywordLoader.getAll()
        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "검색 키워드 초기화 완료: ${keywords.size}개, ${elapsed}ms" }
    }
}
