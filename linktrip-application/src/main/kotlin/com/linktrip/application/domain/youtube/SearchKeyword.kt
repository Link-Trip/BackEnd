package com.linktrip.application.domain.youtube

data class SearchKeyword(
    val query: String,
    val region: String,
    val country: String,
    val city: String? = null,
    val theme: String? = null,
)
