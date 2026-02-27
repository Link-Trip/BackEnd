package com.linktrip.input.http.config

import com.linktrip.input.http.auth.AuthenticatedMemberArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authenticatedMemberArgumentResolver: AuthenticatedMemberArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedMemberArgumentResolver)
    }
}
