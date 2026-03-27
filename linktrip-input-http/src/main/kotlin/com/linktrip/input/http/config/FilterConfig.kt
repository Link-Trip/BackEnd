package com.linktrip.input.http.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.input.http.filter.JwtAuthenticationFilter
import com.linktrip.input.http.filter.MdcLoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {
    @Bean
    fun mdcLoggingFilter(): FilterRegistrationBean<MdcLoggingFilter> =
        FilterRegistrationBean<MdcLoggingFilter>().apply {
            filter = MdcLoggingFilter()
            order = 1
            addUrlPatterns("/*")
        }

    @Bean
    fun jwtAuthenticationFilter(
        tokenProvider: TokenProvider,
        objectMapper: ObjectMapper,
    ): FilterRegistrationBean<JwtAuthenticationFilter> =
        FilterRegistrationBean<JwtAuthenticationFilter>().apply {
            filter = JwtAuthenticationFilter(tokenProvider, objectMapper)
            order = 2
            addUrlPatterns("/*")
        }
}
