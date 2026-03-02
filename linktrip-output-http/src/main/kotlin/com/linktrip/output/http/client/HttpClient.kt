package com.linktrip.output.http.client

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class HttpClient(
    private val restClient: RestClient,
) {
    fun postJson(
        url: String,
        body: Any,
    ) {
        restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity()
    }
}
