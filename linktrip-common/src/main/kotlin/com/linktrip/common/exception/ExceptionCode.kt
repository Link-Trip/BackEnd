package com.linktrip.common.exception

enum class ExceptionCode(
    val statusCode: Int,
    val defaultMessage: String,
) {
    // 400
    BAD_REQUEST_YOUTUBE_URL(400, "유효한 유튜브 URL이 아닙니다."),
    BAD_REQUEST_VIDEO(400, "유효하지 않은 유튜브 영상입니다."),
    BAD_REQUEST_ITINERARY_ITEMS(400, "유효하지 않은 여행 일정 항목입니다."),
    BAD_REQUEST_DISCOVER_QUERY(400, "country와 region은 동시에 사용할 수 없습니다."),

    // 401
    UNAUTHORIZED_TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    UNAUTHORIZED_TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED_TOKEN_MALFORMED(401, "토큰 형식이 올바르지 않습니다."),
    UNAUTHORIZED_AUTHENTICATION_FAILED(401, "인증 정보가 없습니다."),

    // 403
    FORBIDDEN_TRIP_PLAN(403, "해당 여행 계획에 접근할 수 없습니다."),

    // 404
    NOT_FOUND_VIDEO_ANALYSIS_TASK(404, "영상 분석 작업을 찾을 수 없습니다."),
    NOT_FOUND_TRIP_PLAN(404, "여행 계획을 찾을 수 없습니다."),

    // 429
    TOO_MANY_REQUESTS(429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // 500
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    INTERNAL_ERROR_TEST(500, "에러 테스트용 예외입니다."),

    // 502
    BAD_GATEWAY_YOUTUBE(502, "YouTube API 호출 중 오류가 발생했습니다."),
    BAD_GATEWAY_GOOGLE_PLACES(502, "Google Places API 호출 중 오류가 발생했습니다."),
    BAD_GATEWAY_GEMINI(502, "Gemini AI API 호출 중 오류가 발생했습니다."),
    BAD_GATEWAY_DISCORD(502, "Discord API 호출 중 오류가 발생했습니다."),
}
