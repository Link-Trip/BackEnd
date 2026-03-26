package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import java.time.LocalDateTime

data class VideoAnalysisTask(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: VideoAnalysisTaskStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private const val YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch?v="

        /**
         * 지원 URL 형식:
         * - https://www.youtube.com/watch?v=abc123
         * - https://youtube.com/watch?v=abc123 (www 없음)
         * - https://m.youtube.com/watch?v=abc123&si=xxx
         * - https://youtu.be/abc123
         * - https://youtu.be/abc123?si=xxxxx
         * - https://www.youtube.com/embed/abc123
         * - https://www.youtube.com/shorts/abc123
         * - https://www.youtube.com/watch?v=abc123&t=120&si=xxxxx
         */
        private val YOUTUBE_URL_REGEX =
            Regex("^https?://(www\\.|m\\.)?(youtube\\.com/(watch\\?v=|embed/|shorts/)|youtu\\.be/)[\\w-]+.*$")

        /**
         * URL에서 videoId 부분만 추출한다.
         * youtube.com/watch?v=abc123 → abc123
         * youtu.be/abc123 → abc123
         * youtube.com/embed/abc123 → abc123
         * youtube.com/shorts/abc123 → abc123
         */
        private val VIDEO_ID_REGEX =
            Regex("(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([\\w-]+)")

        fun create(youtubeUrl: String): VideoAnalysisTask {
            val normalizedUrl = normalizeUrl(youtubeUrl)
            return VideoAnalysisTask(
                id = IdGenerator.generate(),
                youtubeUrl = normalizedUrl,
                valid = false,
                status = VideoAnalysisTaskStatus.PENDING,
            )
        }

        fun normalizeUrl(url: String): String {
            if (!YOUTUBE_URL_REGEX.matches(url)) {
                throw LinktripException(ExceptionCode.BAD_REQUEST_YOUTUBE_URL)
            }
            val videoId =
                VIDEO_ID_REGEX.find(url)?.groupValues?.get(1)
                    ?: throw LinktripException(ExceptionCode.BAD_REQUEST_YOUTUBE_URL)
            return "$YOUTUBE_VIDEO_BASE_URL$videoId"
        }

        fun buildUrl(videoId: String): String = "$YOUTUBE_VIDEO_BASE_URL$videoId"
    }
}
