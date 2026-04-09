package com.linktrip.application.domain.youtube

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SearchKeywordTest {
    @Test
    fun `pickRandom을 호출하면_전체 키워드 목록에서 정확히 5개를 선택한다`() {
        // given - JSON 파일에서 로드된 키워드 목록이 존재하는 상태

        // when - pickRandom을 호출한다
        val picked = SearchKeywordLoader.pickRandom()

        // then - 정확히 5개의 키워드가 선택된다
        assertEquals(5, picked.size)
    }

    @Test
    fun `pickRandom으로 선택된 키워드는_모두 로드된 키워드 목록에 포함되어 있다`() {
        // given - JSON 파일에서 로드된 키워드 목록이 존재하는 상태
        val allKeywords = SearchKeywordLoader.getAll()

        // when - pickRandom을 호출한다
        val picked = SearchKeywordLoader.pickRandom()

        // then - 선택된 모든 키워드가 로드된 목록에 포함되어 있다
        picked.forEach { keyword ->
            assertTrue(allKeywords.contains(keyword)) {
                "Picked keyword $keyword is not in loaded keywords"
            }
        }
    }

    @Test
    fun `JSON 파일에서 로드된 검색 키워드는_pickRandom이 요구하는 최소 5개 이상이다`() {
        // given - JSON 파일에서 로드된 키워드 목록

        // when & then - pickRandom()이 5개를 뽑으므로 최소 5개 이상이어야 한다
        assertTrue(SearchKeywordLoader.getAll().size >= 5) {
            "Loaded keywords must have at least 5 items for pickRandom() to work"
        }
    }

    @Test
    fun `getByRegion으로 아시아를 필터링하면_아시아 키워드만 반환되고_다른 region은 포함되지 않는다`() {
        // when
        val asiaKeywords = SearchKeywordLoader.getByRegion("아시아")

        // then
        assertTrue(asiaKeywords.isNotEmpty())
        asiaKeywords.forEach { assertEquals("아시아", it.region) }
    }

    @Test
    fun `존재하지 않는 region으로 필터링하면_빈 리스트를 반환한다`() {
        assertTrue(SearchKeywordLoader.getByRegion("남극").isEmpty())
    }
}
