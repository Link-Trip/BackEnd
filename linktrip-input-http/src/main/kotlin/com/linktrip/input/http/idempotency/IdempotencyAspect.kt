package com.linktrip.input.http.idempotency

import com.linktrip.application.port.output.idempotency.IdempotencyStatus
import com.linktrip.application.port.output.idempotency.IdempotencyStore
import com.linktrip.common.annotation.Idempotent
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.filter.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class IdempotencyAspect(
    private val idempotencyStore: IdempotencyStore,
) {
    @Around("@annotation(idempotent)")
    fun checkIdempotency(
        joinPoint: ProceedingJoinPoint,
        idempotent: Idempotent,
    ): Any? {
        val request = currentRequest() ?: return joinPoint.proceed()
        val key = buildScopedKey(request) ?: return joinPoint.proceed()

        return when (idempotencyStore.find(key)?.status) {
            IdempotencyStatus.PROCESSING -> {
                logger.info { "멱등성 키 중복 요청 차단 (처리 중): key=$key" }
                throw LinktripException(ExceptionCode.DUPLICATE_REQUEST)
            }

            IdempotencyStatus.COMPLETED -> {
                logger.info { "멱등성 키 캐시 응답 반환: key=$key" }
                idempotencyStore.find(key)!!.body
            }

            IdempotencyStatus.FAILED, null -> execute(key, joinPoint)
        }
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
            val result = joinPoint.proceed()
            idempotencyStore.saveCompleted(key, result)
            result
        } catch (t: Throwable) {
            idempotencyStore.saveFailed(key)
            throw t
        }
    }

    private fun buildScopedKey(request: HttpServletRequest): String? {
        val rawKey =
            request.getHeader(IDEMPOTENCY_KEY_HEADER)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return null

        val memberId =
            request.getAttribute(JwtAuthenticationFilter.MEMBER_ID_ATTRIBUTE) as? String
                ?: "anonymous"

        return "${request.method}:${request.requestURI}:$memberId:$rawKey"
    }

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    companion object {
        private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
    }
}
