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

/**
 * webshare 프록시 라운드로빈 클라이언트.
 *
 * [proxyClients] 에 지정된 순서대로 각 프록시로 요청을 시도한다 (우선순위 = 리스트 순서).
 * - 2xx → 성공 반환
 * - 429/403 (IP 차단) → 다음 프록시 시도
 * - 5xx / 기타 비2xx → 즉시 [LinktripException] throw (일시 오류는 큐 컨슈머가 PENDING 으로 재시도)
 *
 * 전체 프록시가 429/403 으로 소진되면 IP 전면 차단으로 간주하고 [LinktripException] throw.
 */
class ProxyYoutubeClient(
    usernames: List<String>,
    password: String,
) : YoutubeClient {
    private data class ProxyClient(val username: String, val httpClient: HttpClient)

    private val proxyClients: List<ProxyClient> =
        usernames.map { username ->
            ProxyClient(username, buildHttpClient(username, password))
        }

    override fun get(
        url: String,
        headers: Map<String, String>,
    ): String {
        val builder = HttpRequest.newBuilder().uri(URI.create(url)).GET()
        headers.forEach { (key, value) -> builder.header(key, value) }
        return executeWithRotation(builder.build(), url)
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
        return executeWithRotation(request, url)
    }

    private fun executeWithRotation(
        request: HttpRequest,
        url: String,
    ): String {
        var lastBlockedStatus = -1
        for (proxy in proxyClients) {
            val response = proxy.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val status = response.statusCode()
            val body = response.body() ?: ""

            logger.debug { "프록시 요청: username=${proxy.username}, url=$url, status=$status, bytes=${body.length}" }

            when (status) {
                in 200..299 -> return body
                429, 403 -> {
                    logger.info { "프록시 IP 차단으로 다음 프록시 시도: username=${proxy.username}, status=$status" }
                    lastBlockedStatus = status
                }

                in 500..599 ->
                    throw LinktripException(
                        ExceptionCode.BAD_GATEWAY_YOUTUBE,
                        "YouTube 일시 오류: status=$status",
                    )

                else -> throw LinktripException(
                    ExceptionCode.BAD_GATEWAY_YOUTUBE,
                    "예상치 못한 HTTP 응답: status=$status",
                )
            }
        }
        throw LinktripException(
            ExceptionCode.BAD_GATEWAY_YOUTUBE,
            "전 프록시 IP 차단 확정 (마지막 status=$lastBlockedStatus, proxies=${proxyClients.size}개)",
        )
    }

    private fun buildHttpClient(
        username: String,
        password: String,
    ): HttpClient =
        HttpClient.newBuilder()
            .proxy(ProxySelector.of(InetSocketAddress(PROXY_HOST, PROXY_PORT)))
            .authenticator(
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication =
                        PasswordAuthentication(username, password.toCharArray())
                },
            )
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    companion object {
        private const val PROXY_HOST = "p.webshare.io"
        private const val PROXY_PORT = 80

        init {
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "")
        }
    }
}
