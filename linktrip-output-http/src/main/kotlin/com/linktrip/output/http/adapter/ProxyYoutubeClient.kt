package com.linktrip.output.http.adapter

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import io.github.thoroldvix.api.YoutubeClient
import mu.KotlinLogging
import java.io.IOException
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * webshare 프록시 라운드로빈 클라이언트.
 *
 * [proxyClients] 에 지정된 순서대로 각 프록시로 요청을 시도한다 (우선순위 = 리스트 순서).
 * - 2xx → 성공 반환
 * - 429/403 (IP 차단) → 다음 프록시 시도
 * - IOException (DNS/연결/읽기 타임아웃 등 네트워크 장애) → 다음 프록시 시도
 * - 5xx / 기타 비2xx → 즉시 [LinktripException] throw (일시 오류는 큐 컨슈머가 PENDING 으로 재시도)
 *
 * 모든 프록시가 차단/네트워크 장애로 소진되면 [LinktripException] throw.
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
        val builder =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
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
                .timeout(REQUEST_TIMEOUT)
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
        var lastNetworkFailure: String? = null
        for (proxy in proxyClients) {
            val response =
                try {
                    proxy.httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                } catch (e: IOException) {
                    // 네트워크 장애 (DNS, 연결 거부, 타임아웃 등) → 라운드로빈 의도대로 다음 프록시로
                    logger.warn(e) { "프록시 통신 실패로 다음 프록시 시도: username=${proxy.username}" }
                    lastNetworkFailure = e.message ?: e::class.simpleName
                    continue
                } catch (_: InterruptedException) {
                    // 컨슈머 스레드 중단 신호 — interrupt flag 복원 후 종료
                    Thread.currentThread().interrupt()
                    throw LinktripException(
                        ExceptionCode.BAD_GATEWAY_YOUTUBE,
                        "프록시 요청 중단됨",
                    )
                }
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
            "전 프록시 소진 (마지막 차단=$lastBlockedStatus, 마지막 네트워크 실패=$lastNetworkFailure, " +
                "proxies=${proxyClients.size}개)",
        )
    }

    private fun buildHttpClient(
        username: String,
        password: String,
    ): HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
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

        /** 프록시까지의 TCP 연결 수립 타임아웃. 죽은 프록시에서 무한 대기 방지. */
        private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)

        /**
         * YouTube 응답까지의 전체 요청 타임아웃.
         *
         * 긴 영상(1시간+) 의 자막 XML 다운로드 + 해외 프록시 RTT 까지 감안해서 넉넉히 60초.
         * 너무 짧으면 멀쩡한 프록시도 false-timeout 으로 다음 프록시 시도하게 되어 라운드로빈 가속만 됨.
         * 그래도 무한 대기는 방지 (느린 프록시가 컨슈머 스레드 점유 안 하도록).
         */
        private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(60)

        init {
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "")
        }
    }
}
