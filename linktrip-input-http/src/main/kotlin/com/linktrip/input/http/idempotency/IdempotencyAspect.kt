package com.linktrip.input.http.idempotency

import com.linktrip.application.port.output.idempotency.IdempotencyStore
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.filter.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class IdempotencyAspect(
    private val idempotencyStore: IdempotencyStore,
) {
    @Pointcut(
        "@within(org.springframework.web.bind.annotation.RestController) && execution(public * *(..)) && " +
            "!within(com.linktrip.input.http.controller.HealthCheckController) && " +
            "!within(com.linktrip.input.http.controller.YouTubeTestController)",
    )
    fun nonGetRequestInProtectedController() {
    }

    @Around("nonGetRequestInProtectedController()")
    fun checkIdempotency(joinPoint: ProceedingJoinPoint): Any? {
        val request = currentRequest() ?: return joinPoint.proceed()
        if (request.method == GET_METHOD) {
            return joinPoint.proceed()
        }

        val key = buildScopedKey(request)
        return execute(key, joinPoint)
    }

    private fun execute(
        key: String,
        joinPoint: ProceedingJoinPoint,
    ): Any? {
        if (!idempotencyStore.tryLock(key)) {
            logger.info { "멱등성 키 락 획득 실패: key=$key" }
            throw LinktripException(ExceptionCode.DUPLICATE_REQUEST)
        }

        return try {
            joinPoint.proceed()
        } catch (t: Throwable) {
            throw t
        } finally {
            idempotencyStore.release(key)
        }
    }

    private fun buildScopedKey(request: HttpServletRequest): String {
        val rawKey =
            request.getHeader(IDEMPOTENCY_KEY_HEADER)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw LinktripException(ExceptionCode.BAD_REQUEST_MISSING_IDEMPOTENCY_KEY)

        val memberId =
            request.getAttribute(JwtAuthenticationFilter.MEMBER_ID_ATTRIBUTE) as? String
                ?: "anonymous"

        return "${request.method}:${request.requestURI}:$memberId:$rawKey"
    }

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    companion object {
        private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
        private const val GET_METHOD = "GET"
    }
}
