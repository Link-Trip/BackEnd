package com.linktrip.input.http.controller.advice

import com.linktrip.application.domain.notification.ExceptionAlertEvent
import com.linktrip.common.config.event.Events
import com.linktrip.common.exception.LinktripException
import com.linktrip.input.http.controller.dto.response.ExceptionResponse
import jakarta.validation.ConstraintViolationException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class ExceptionAdvice {
    @ExceptionHandler(LinktripException::class)
    fun handleLinktripException(e: LinktripException): ResponseEntity<ExceptionResponse> {
        if (e.statusCode in 500..599) {
            logger.error(e) { "LinktripException 발생 (${e.statusCode})" }
            raiseExceptionAlert(
                message = e.defaultMessage,
                cause = e.detailMessage,
                statusCode = e.statusCode,
                stackTrace = e.stackTraceToString(),
            )
        } else {
            logger.warn { "LinktripException 발생 (${e.statusCode}) - ${e.defaultMessage}" }
        }
        return ResponseEntity
            .status(e.statusCode)
            .body(
                ExceptionResponse(
                    message = e.defaultMessage,
                    cause = e.detailMessage,
                    timestamp = System.currentTimeMillis(),
                ),
            )
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
    )
    fun handleValidationException(e: Exception): ResponseEntity<ExceptionResponse> {
        logger.warn { "Validation 실패 - ${e.message}" }

        val cause =
            when (e) {
                is MethodArgumentNotValidException ->
                    e.bindingResult.fieldErrors.joinToString(", ") {
                        "${it.field}: ${it.defaultMessage}"
                    }

                is ConstraintViolationException ->
                    e.constraintViolations.joinToString(", ") {
                        "${it.propertyPath}: ${it.message}"
                    }

                else -> e.message
            }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ExceptionResponse(
                    message = "요청 형식이 잘못되었습니다.",
                    cause = cause,
                    timestamp = System.currentTimeMillis(),
                ),
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(e: NoResourceFoundException): ResponseEntity<ExceptionResponse> {
        logger.warn { "리소스를 찾을 수 없음 - ${e.resourcePath}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ExceptionResponse(
                    message = "요청 경로가 잘못되었습니다.",
                    cause = e.resourcePath,
                    timestamp = System.currentTimeMillis(),
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ExceptionResponse> {
        logger.error(e) { "예기치 못한 에러 발생" }
        raiseExceptionAlert(
            message = "예기치 못한 에러가 발생했습니다.",
            cause = e.message,
            statusCode = 500,
            stackTrace = e.stackTraceToString(),
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ExceptionResponse(
                    message = "예기치 못한 에러가 발생했습니다.",
                    cause = null,
                    timestamp = System.currentTimeMillis(),
                ),
            )
    }

    private fun raiseExceptionAlert(
        message: String,
        cause: String?,
        statusCode: Int,
        stackTrace: String?,
    ) {
        Events.raise(
            ExceptionAlertEvent(
                message = message,
                cause = cause,
                statusCode = statusCode,
                stackTrace = stackTrace,
            ),
        )
    }
}
