package com.linktrip.input.http.controller.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun ok(): ApiResponse<Unit> = ApiResponse(status = 200, message = "OK")

        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(status = 200, message = "OK", data = data)

        fun <T> created(data: T): ApiResponse<T> = ApiResponse(status = 201, message = "Created", data = data)
    }
}
