package com.linktrip.input.http.auth

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.filter.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedMemberArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthenticatedMember::class.java) &&
            parameter.parameterType == String::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): String {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw LinktripException(ExceptionCode.INTERNAL_SERVER_ERROR)

        return request.getAttribute(JwtAuthenticationFilter.MEMBER_ID_ATTRIBUTE) as? String
            ?: throw LinktripException(ExceptionCode.UNAUTHORIZED_AUTHENTICATION_FAILED)
    }
}
