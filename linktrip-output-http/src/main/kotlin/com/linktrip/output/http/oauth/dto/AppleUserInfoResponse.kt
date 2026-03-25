package com.linktrip.output.http.oauth.dto

data class ApplePublicKeyResponse(
    val keys: List<AppleKey>,
) {
    data class AppleKey(
        val kty: String,
        val kid: String,
        val use: String,
        val alg: String,
        val n: String,
        val e: String,
    )
}
