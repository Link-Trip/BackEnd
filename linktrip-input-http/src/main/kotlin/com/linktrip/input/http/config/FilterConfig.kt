package com.linktrip.input.http.config

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
}
