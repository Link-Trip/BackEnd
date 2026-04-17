package com.linktrip.output.http.adapter

import com.linktrip.output.http.properties.YouTubeProperties
import io.github.thoroldvix.api.TranscriptApiFactory
import io.github.thoroldvix.api.YoutubeTranscriptApi
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * youtube-transcript-api 라이브러리에 대한 단일 게이트웨이.
 *
 * - 자막 추출 (요약 흐름)
 * - sentinel ping 을 통한 프록시 헬스 체크 (모호한 실패 분류)
 *
 * 두 흐름 모두 같은 [transcriptApi] 인스턴스를 통과하므로 프록시 설정/HTTP 동작이 일관된다.
 */
@Component
class YoutubeTranscriptClient(
    private val youTubeProperties: YouTubeProperties,
) {
    private val transcriptApi: YoutubeTranscriptApi by lazy {
        if (youTubeProperties.proxy.isEnabled()) {
            logger.info { "YouTube 자막 프록시 활성화 (prod)" }
            TranscriptApiFactory.createWithClient(
                ProxyYoutubeClient(
                    youTubeProperties.proxy.username,
                    youTubeProperties.proxy.password,
                ),
            )
        } else {
            logger.info { "YouTube 자막 직접 연결 (dev)" }
            TranscriptApiFactory.createDefault()
        }
    }

    /**
     * 자막 텍스트 추출. ko 수동 → en 수동 → ko 자동 → en 자동 순으로 폴백.
     *
     * @return 포맷팅된 자막 텍스트, 어느 언어에도 자막이 없으면 null
     * @throws com.linktrip.common.exception.LinktripException 프록시/HTTP 레벨 실패 (ProxyYoutubeClient 가 throw)
     * @throws io.github.thoroldvix.api.TranscriptRetrievalException 라이브러리가 던지는 모호한 실패
     */
    fun fetchTranscript(videoId: String): String? {
        val list = transcriptApi.listTranscripts(videoId)
        val transcript =
            runCatching { list.findTranscript("ko") }
                .recoverCatching { list.findTranscript("en") }
                .recoverCatching { list.findGeneratedTranscript("ko") }
                .recoverCatching { list.findGeneratedTranscript("en") }
                .getOrNull()

        return transcript?.fetch()?.let { content ->
            content.content.joinToString("\n") { fragment ->
                "[${formatTimestamp(fragment.start.toLong())}] ${fragment.text}"
            }
        }
    }

    /**
     * 프록시(IP) 가 정상인지 sentinel 영상으로 매번 즉시 확인.
     *
     * 큐 컨슈머가 이미 자연스러운 rate limit 이라 호출 빈도가 낮고, IP 차단은 실시간 상태가 중요하므로 캐시 없음.
     *
     * @return true = 프록시 정상, false = 차단 의심 (또는 sentinel 미설정)
     */
    fun isProxyHealthy(): Boolean {
        val sentinelId = youTubeProperties.healthCheck.sentinelVideoId
        if (sentinelId.isBlank()) {
            logger.warn { "Sentinel videoId 미설정 — 프록시 비정상으로 간주" }
            return false
        }

        val healthy =
            runCatching {
                transcriptApi.listTranscripts(sentinelId)
                true
            }.getOrElse { e ->
                logger.warn(e) { "Sentinel ping 실패: videoId=$sentinelId" }
                false
            }

        logger.info { "프록시 헬스체크 결과: healthy=$healthy (sentinelId=$sentinelId)" }
        return healthy
    }

    companion object {
        private fun formatTimestamp(totalSeconds: Long): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }
    }
}
