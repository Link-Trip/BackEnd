package com.linktrip.output.http.oauth.adapter

import com.linktrip.application.domain.member.ProviderType
import com.linktrip.application.port.output.auth.OAuthInfo
import com.linktrip.application.port.output.auth.OAuthPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.http.oauth.dto.KakaoUserInfoResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private val logger = KotlinLogging.logger {}

@Component
class KakaoOAuthAdapter(
    @param:Qualifier("kakaoOAuthRestClient") private val restClient: RestClient,
) : OAuthPort {
    override fun getProviderType(): ProviderType = ProviderType.KAKAO

    override fun requestUserInfo(accessToken: String): OAuthInfo {
        val response =
            restClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body<KakaoUserInfoResponse>()
                ?: throw LinktripException(ExceptionCode.BAD_GATEWAY_OAUTH_PROVIDER)

        logger.debug { "Kakao 사용자 정보 조회 성공: id=${response.id}" }

        return OAuthInfo(
            providerType = ProviderType.KAKAO,
            providerId = response.id.toString(),
            email = response.kakaoAccount?.email,
        )
    }
}
