package com.linktrip.input.http.ratelimit

import com.linktrip.application.port.output.ratelimit.RateLimitBucketStore
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val rateLimitBucketStore: RateLimitBucketStore,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val memberId =
            SecurityContextHolder.getContext().authentication?.principal as? String
                ?: return true

        val policy =
            RateLimitPathResolver.resolve(request.requestURI)
                ?: return true

        val key = "$memberId:${policy.name}"

        if (!rateLimitBucketStore.tryConsume(key, policy)) {
            throw LinktripException(ExceptionCode.TOO_MANY_REQUESTS)
        }

        return true
    }
}
