package com.linktrip.output.http.adapter

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
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

        return execute(requestBuilder.build(), url)
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

        return execute(request, url)
    }

    private fun execute(
        request: HttpRequest,
        url: String,
    ): String {
        val response =
            client.send(
                request,
                HttpResponse.BodyHandlers.ofString(),
            )
        val status = response.statusCode()
        val body = response.body() ?: ""

        logger.debug { "프록시 요청: url=$url, status=$status, bytes=${body.length}" }

        throwIfFailureStatus(status)
        return body
    }

    private fun throwIfFailureStatus(status: Int) {
        when {
            status == 429 || status == 403 ->
                throw LinktripException(
                    ExceptionCode.BAD_GATEWAY_YOUTUBE,
                    "프록시 IP 차단 의심: status=$status",
                )
            status in 500..599 ->
                throw LinktripException(
                    ExceptionCode.BAD_GATEWAY_YOUTUBE,
                    "YouTube 일시 오류: status=$status",
                )
            status !in 200..299 ->
                throw LinktripException(
                    ExceptionCode.BAD_GATEWAY_YOUTUBE,
                    "예상치 못한 HTTP 응답: status=$status",
                )
        }
    }

    companion object {
        private const val PROXY_HOST = "p.webshare.io"
        private const val PROXY_PORT = 80

        init {
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "")
        }
    }
}
