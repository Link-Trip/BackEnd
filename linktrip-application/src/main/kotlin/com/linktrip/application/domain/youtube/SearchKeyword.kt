package com.linktrip.application.domain.youtube

data class SearchKeyword(
    val query: String,
    val region: String,
    val country: String,
    val city: String? = null,
    val theme: String? = null,
) {
    companion object {
        val KEYWORDS: List<SearchKeyword> =
            listOf(
                // 아시아 - 일본
                SearchKeyword("도쿄 여행 브이로그", "아시아", "일본", "도쿄"),
                SearchKeyword("오사카 여행 브이로그", "아시아", "일본", "오사카"),
                SearchKeyword("교토 여행 브이로그", "아시아", "일본", "교토"),
                SearchKeyword("일본 여행 브이로그", "아시아", "일본"),
                // 아시아 - 태국
                SearchKeyword("방콕 여행 브이로그", "아시아", "태국", "방콕"),
                SearchKeyword("태국 여행 브이로그", "아시아", "태국"),
                // 아시아 - 베트남
                SearchKeyword("베트남 여행 브이로그", "아시아", "베트남"),
                SearchKeyword("다낭 여행 브이로그", "아시아", "베트남", "다낭"),
                // 아시아 - 홍콩
                SearchKeyword("홍콩 여행 브이로그", "아시아", "홍콩", "홍콩"),
                // 유럽 - 프랑스
                SearchKeyword("파리 여행 브이로그", "유럽", "프랑스", "파리"),
                SearchKeyword("프랑스 여행 브이로그", "유럽", "프랑스"),
                // 유럽 - 이탈리아
                SearchKeyword("로마 여행 브이로그", "유럽", "이탈리아", "로마"),
                SearchKeyword("이탈리아 여행 브이로그", "유럽", "이탈리아"),
                // 유럽 - 스페인
                SearchKeyword("바르셀로나 여행 브이로그", "유럽", "스페인", "바르셀로나"),
                SearchKeyword("스페인 여행 브이로그", "유럽", "스페인"),
                // 유럽 - 영국
                SearchKeyword("런던 여행 브이로그", "유럽", "영국", "런던"),
                // 북아메리카 - 미국
                SearchKeyword("뉴욕 여행 브이로그", "북아메리카", "미국", "뉴욕"),
                SearchKeyword("하와이 여행 브이로그", "북아메리카", "미국", "하와이"),
                SearchKeyword("LA 여행 브이로그", "북아메리카", "미국", "LA"),
                // 동남아 - 인도네시아
                SearchKeyword("발리 여행 브이로그", "아시아", "인도네시아", "발리"),
                // 테마
                SearchKeyword("미식 여행 맛집 브이로그", "전체", "전체", theme = "미식"),
                SearchKeyword("힐링 여행 자연 브이로그", "전체", "전체", theme = "힐링"),
                SearchKeyword("도심 여행 도시 브이로그", "전체", "전체", theme = "도심"),
                SearchKeyword("자연 여행 트레킹 브이로그", "전체", "전체", theme = "자연"),
            )

        private const val RANDOM_PICK_COUNT = 5

        fun pickRandom(): List<SearchKeyword> = KEYWORDS.shuffled().take(RANDOM_PICK_COUNT)
    }
}
