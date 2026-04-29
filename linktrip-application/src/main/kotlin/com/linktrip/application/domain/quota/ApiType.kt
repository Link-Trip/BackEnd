package com.linktrip.application.domain.quota

/**
 * 비용 통제가 필요한 외부 API 종류.
 * 새 외부 API 도입 시 enum 값 + properties 의 daily-quota / cost-per-call 항목 추가.
 */
enum class ApiType {
    /** Google Vertex AI Gemini — 토큰 기반 종량제 */
    GEMINI,

    /** YouTube Data API v3 — 일일 quota */
    YOUTUBE_DATA,

    /** Google Places Text Search — 호출당 과금 */
    GOOGLE_PLACES,
}
