package com.linktrip.output.http.adapter

import io.github.thoroldvix.api.YoutubeClient
import mu.KotlinLogging
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val logger = KotlinLogging.logger {}

class ProxyYoutubeClient(
    private val proxyUsername: String,
    private val proxyPassword: String,
) : YoutubeClient {
    private val client: HttpClient by lazy {
        HttpClient.newBuilder()
            .proxy(
                ProxySelector.of(
                    InetSocketAddress(PROXY_HOST, PROXY_PORT),
                ),
            )
            .authenticator(
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication =
                        PasswordAuthentication(proxyUsername, proxyPassword.toCharArray())
                },
            )
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    override fun get(
        url: String,
        headers: Map<String, String>,
    ): String {
        val requestBuilder =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()

        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val response =
            client.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString(),
            )

        logger.debug { "프록시 요청: url=$url, status=${response.statusCode()}" }
        return response.body()
    }

    override fun post(
        url: String,
        json: String,
    ): String {
        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build()

        val response =
            client.send(
                request,
                HttpResponse.BodyHandlers.ofString(),
            )

        return response.body()
    }

    companion object {
        private const val PROXY_HOST = "p.webshare.io"
        private const val PROXY_PORT = 80
    }
}
