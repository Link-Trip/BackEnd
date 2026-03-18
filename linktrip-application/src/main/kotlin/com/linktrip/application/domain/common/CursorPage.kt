package com.linktrip.application.domain.common

data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
)
