package com.linktrip.application.port.output.auth

interface TokenProvider {
    fun create(memberId: String): String

    fun validate(token: String): Boolean

    fun extractMemberId(token: String): String
}
