package com.linktrip.input.http.controller.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExceptionResponse(
    val code: String?,
    val message: String?,
    val cause: String?,
    val timestamp: Long,
)
