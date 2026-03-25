package com.linktrip.output.http.oauth.config

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Configuration
class OAuthRestClientConfig {
    @Bean("kakaoOAuthRestClient")
    fun kakaoOAuthRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(KAKAO_USER_INFO_URL)
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "Kakao API 호출 실패: status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.OAUTH_PROVIDER_ERROR)
            }
            .requestFactory(clientHttpRequestFactory())
            .build()

    @Bean("googleOAuthRestClient")
    fun googleOAuthRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(GOOGLE_USER_INFO_URL)
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "Google API 호출 실패: status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.OAUTH_PROVIDER_ERROR)
            }
            .requestFactory(clientHttpRequestFactory())
            .build()

    @Bean("appleOAuthRestClient")
    fun appleOAuthRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(APPLE_BASE_URL)
            .defaultStatusHandler({ it.isError }) { request, response ->
                logger.error { "Apple API 호출 실패: status=${response.statusCode}" }
                throw LinktripException(ExceptionCode.OAUTH_PROVIDER_ERROR)
            }
            .requestFactory(clientHttpRequestFactory())
            .build()

    private fun clientHttpRequestFactory() =
        SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(5))
            setReadTimeout(Duration.ofSeconds(10))
        }

    companion object {
        private const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com"
        private const val GOOGLE_USER_INFO_URL = "https://www.googleapis.com"
        private const val APPLE_BASE_URL = "https://appleid.apple.com"
    }
}
