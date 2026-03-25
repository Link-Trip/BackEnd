package com.linktrip.output.http.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,
    @field:JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?,
) {
    data class KakaoAccount(
        val email: String?,
    )
}
