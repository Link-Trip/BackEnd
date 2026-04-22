package com.linktrip.application.domain.video

import com.linktrip.common.exception.LinktripException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VideoAnalysisTaskTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://www.youtube.com/watch?v=abc123",
            "https://youtube.com/watch?v=abc123",
            "https://m.youtube.com/watch?v=abc123",
            "https://youtu.be/abc123",
            "https://youtu.be/abc123?si=xxxxx",
            "https://www.youtube.com/embed/abc123",
            "https://www.youtube.com/shorts/abc123",
            "https://www.youtube.com/watch?v=abc123&t=120",
            "https://www.youtube.com/watch?v=abc123&si=xxxxx&t=120",
        ],
    )
    fun `다양한 형태의 YouTube URL이 모두 동일한 정규화된 URL로 변환된다`(url: String) {
        // when
        val normalized = VideoAnalysisTask.normalizeUrl(url)

        // then
        assertEquals("https://www.youtube.com/watch?v=abc123", normalized)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://naver.com/video",
            "https://vimeo.com/123",
            "not-a-url",
            "",
            "https://notyoutube.com/watch?v=abc123",
            "youtube.com/watch?v=abc123",
        ],
    )
    fun `유효하지 않은 URL은 예외가 발생한다`(url: String) {
        assertThrows<LinktripException> {
            VideoAnalysisTask.normalizeUrl(url)
        }
    }

    @Test
    fun `buildUrl은 videoId로 표준 YouTube URL을 생성한다`() {
        // when
        val url = VideoAnalysisTask.buildUrl("abc123")

        // then
        assertEquals("https://www.youtube.com/watch?v=abc123", url)
    }

    @Test
    fun `create는 정규화된 URL로 PENDING 상태와 지정된 source로 VideoAnalysisTask를 생성한다`() {
        // given - youtu.be 단축 URL + USER source
        val shortUrl = "https://youtu.be/abc123"

        // when
        val task = VideoAnalysisTask.create(shortUrl, Source.USER)

        // then - 정규화된 URL, PENDING 상태, valid=false, source=USER
        assertEquals("https://www.youtube.com/watch?v=abc123", task.youtubeUrl)
        assertEquals(VideoAnalysisTaskStatus.PENDING, task.status)
        assertEquals(Source.USER, task.source)
        assertFalse(task.valid)
        assertTrue(task.id.isNotBlank())
    }

    @Test
    fun `create에 BATCH source를 전달하면_source=BATCH로 task가 생성되어 통계 분류가 가능하다`() {
        // when - BATCH source 로 생성
        val task = VideoAnalysisTask.create("https://youtu.be/abc123", Source.BATCH)

        // then - source=BATCH 로 audit 정보가 박혀 통계 집계 가능
        assertEquals(Source.BATCH, task.source)
    }

    @Test
    fun `create에 유효하지 않은 URL을 전달하면 예외가 발생한다`() {
        assertThrows<LinktripException> {
            VideoAnalysisTask.create("https://naver.com/video", Source.USER)
        }
    }
}
