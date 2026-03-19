package com.linktrip.application.domain.video

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VideoScheduleItemTest {
    @Test
    fun `장소 미확정이고_교통편이 아니고_검색 횟수가 10회 미만이면_재검색 가능 상태이다`() {
        // given - placeId가 null이고, EAT 카테고리이고, 검색 횟수가 0인 항목
        val item = createItem(placeId = null, category = Category.EAT, placeSearchCount = 0)

        // when - 재검색 가능 여부를 확인한다
        // then - retryable이고, resolved가 아니다
        assertTrue(item.isRetryable())
        assertFalse(item.isResolved())
    }

    @Test
    fun `이미 장소가 확정된 항목은_재검색 대상이 아니다`() {
        // given - placeId가 존재하는 항목
        val item = createItem(placeId = "place-1", category = Category.EAT, placeSearchCount = 0)

        // when - 재검색 가능 여부를 확인한다
        // then - retryable이 아니고, resolved 상태이다
        assertFalse(item.isRetryable())
        assertTrue(item.isResolved())
    }

    @Test
    fun `교통편 카테고리는_장소 검색이 필요 없으므로_재검색 대상이 아니다`() {
        // given - TRANSPORTATION 카테고리인 항목
        val item = createItem(placeId = null, category = Category.TRANSPORTATION, placeSearchCount = 0)

        // when - 재검색 가능 여부를 확인한다
        // then - retryable이 아니다
        assertFalse(item.isRetryable())
    }

    @Test
    fun `장소 검색을 10회 이상 시도한 항목은_더 이상 재검색하지 않는다`() {
        // given - 검색 횟수가 10인 항목
        val item = createItem(placeId = null, category = Category.ATTRACTION, placeSearchCount = 10)

        // when - 재검색 가능 여부를 확인한다
        // then - retryable이 아니다
        assertFalse(item.isRetryable())
    }

    @Test
    fun `장소 검색을 9회 시도한 항목은_아직 재검색 가능하다`() {
        // given - 검색 횟수가 9인 항목
        val item = createItem(placeId = null, category = Category.ATTRACTION, placeSearchCount = 9)

        // when - 재검색 가능 여부를 확인한다
        // then - retryable이다
        assertTrue(item.isRetryable())
    }

    @Test
    fun `AI 분석 결과를 VideoScheduleItem으로 변환하면_day와 category와 name 등 모든 필드가 정확히 매핑된다`() {
        // given - VideoAnalysisResult의 DaySchedule과 ScheduleItem
        val daySchedule = VideoAnalysisResult.DaySchedule(
            day = 2,
            items = emptyList(),
        )
        val scheduleItem = VideoAnalysisResult.ScheduleItem(
            order = 3,
            category = Category.SHOPPING,
            name = "명동 쇼핑",
            description = "한국 화장품 쇼핑",
            tips = "카드 결제 가능",
        )

        // when - from 메서드로 변환한다
        val result = VideoScheduleItem.from("summary-1", daySchedule, scheduleItem)

        // then - 모든 필드가 정확히 매핑된다
        assertNotNull(result.id)
        assertEquals("summary-1", result.videoSummaryId)
        assertEquals(2, result.day)
        assertEquals(3, result.itemOrder)
        assertEquals(Category.SHOPPING, result.category)
        assertEquals("명동 쇼핑", result.name)
        assertEquals("한국 화장품 쇼핑", result.description)
        assertEquals("카드 결제 가능", result.tips)
        assertNull(result.placeId)
        assertEquals(0, result.placeSearchCount)
        assertNull(result.place)
    }

    private fun createItem(
        placeId: String? = null,
        category: Category = Category.EAT,
        placeSearchCount: Int = 0,
    ) = VideoScheduleItem(
        id = "item-1",
        videoSummaryId = "summary-1",
        day = 1,
        itemOrder = 1,
        category = category,
        name = "테스트 장소",
        description = null,
        tips = null,
        placeId = placeId,
        placeSearchCount = placeSearchCount,
    )
}
