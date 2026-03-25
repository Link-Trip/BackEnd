package com.linktrip.input.http.security

import org.springframework.security.authentication.AbstractAuthenticationToken

class PostAuthorizationToken(
    private val memberId: String,
) : AbstractAuthenticationToken(emptyList()) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): String = memberId
}
