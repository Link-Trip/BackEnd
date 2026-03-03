package com.linktrip.output.http.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gcp")
data class GcpProperties(
    val projectId: String,
    val credentialsPath: String,
    val vertexAi: VertexAi = VertexAi(),
) {
    data class VertexAi(
        val location: String = "us-central1",
    )
}
