package com.linktrip.input.http.filter

object FilterPaths {
    val JWT_WHITELIST =
        listOf(
            "/api/health",
            "/api/auth/login",
            "/api/swagger-ui",
            "/api/api-docs",
            "/api/v3/api-docs",
            "/api/test",
            "/actuator",
        )

    val LOGGING_SKIP =
        listOf(
            "/api/health/",
        )
}
