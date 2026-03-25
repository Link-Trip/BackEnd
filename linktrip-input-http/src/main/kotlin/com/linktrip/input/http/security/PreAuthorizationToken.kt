package com.linktrip.input.http.security

import org.springframework.security.authentication.AbstractAuthenticationToken

class PreAuthorizationToken(
    private val jwt: String,
) : AbstractAuthenticationToken(null) {
    init {
        isAuthenticated = false
    }

    override fun getCredentials(): String = jwt

    override fun getPrincipal(): Any? = null
}
