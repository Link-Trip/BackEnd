package com.linktrip.application.domain.youtube

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SearchKeywordTest {
    @Test
    fun `pickRandom을 호출하면_전체 키워드 목록에서 정확히 5개를 선택한다`() {
        // given - 사전 정의된 키워드 목록이 존재하는 상태

        // when - pickRandom을 호출한다
        val picked = SearchKeyword.pickRandom()

        // then - 정확히 5개의 키워드가 선택된다
        assertEquals(5, picked.size)
    }

    @Test
    fun `pickRandom으로 선택된 키워드는_모두 사전 정의된 KEYWORDS 목록에 포함되어 있다`() {
        // given - 사전 정의된 키워드 목록이 존재하는 상태

        // when - pickRandom을 호출한다
        val picked = SearchKeyword.pickRandom()

        // then - 선택된 모든 키워드가 KEYWORDS 목록에 포함되어 있다
        picked.forEach { keyword ->
            assertTrue(SearchKeyword.KEYWORDS.contains(keyword)) {
                "Picked keyword $keyword is not in KEYWORDS list"
            }
        }
    }

    @Test
    fun `사전 정의된 검색 키워드 목록은_pickRandom이 요구하는 최소 5개 이상이다`() {
        // given - SearchKeyword 클래스의 KEYWORDS 상수

        // when & then - pickRandom()이 5개를 뽑으므로 KEYWORDS는 최소 5개 이상이어야 한다
        assertTrue(SearchKeyword.KEYWORDS.size >= 5) {
            "KEYWORDS must have at least 5 items for pickRandom() to work"
        }
    }
}
