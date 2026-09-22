package com.linktrip.application.domain.terms

import java.time.LocalDateTime

data class Terms(
    val id: String,
    val type: TermsType,
    val title: String,
    val required: Boolean,
    val version: Int,
    val detailUrl: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
