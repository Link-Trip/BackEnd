package com.linktrip.input.http.security

object SecurityPaths {
    val PUBLIC_ENDPOINTS =
        arrayOf(
            "/auth/login/**",
            "/health/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/test/**",
            "/actuator/**",
        )
}
