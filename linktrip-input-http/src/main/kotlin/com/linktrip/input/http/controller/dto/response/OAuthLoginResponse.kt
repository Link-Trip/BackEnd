package com.linktrip.input.http.controller.dto.response

data class OAuthLoginResponse(
    val memberId: String,
    val accessToken: String,
    val isNewMember: Boolean,
)
