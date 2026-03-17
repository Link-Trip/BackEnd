package com.linktrip.common.exception

enum class ExceptionCode(
    val statusCode: Int,
    val defaultMessage: String,
) {
    // 400
    ILLEGAL_ARGUMENT(400, "요청 값이 올바르지 않습니다."),
    INVALID_YOUTUBE_URL(400, "유효한 유튜브 URL이 아닙니다."),
    INVALID_VIDEO(400, "유효하지 않은 유튜브 영상입니다."),

    // 401
    UNAUTHORIZED(401, "인증이 필요합니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),
    TOKEN_MALFORMED(401, "토큰 형식이 올바르지 않습니다."),
    AUTHENTICATION_FAILED(401, "인증 정보가 없습니다."),

    // 403
    ACCESS_DENIED(403, "해당 리소스에 접근할 수 없습니다."),

    // 404
    NOT_FOUND(404, "해당 리소스를 찾을 수 없습니다."),

    // 409
    CONFLICT(409, "해당 리소스가 중복됩니다."),

    // 500
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    ERROR_TEST(500, "에러 테스트용 예외입니다."),

    // 502
    API_ERROR_YOUTUBE(502, "YouTube API 호출 중 오류가 발생했습니다."),
    API_ERROR_GOOGLE_PLACES(502, "Google Places API 호출 중 오류가 발생했습니다."),
    API_ERROR_GEMINI(502, "Gemini AI API 호출 중 오류가 발생했습니다."),
    API_ERROR_DISCORD(502, "Discord API 호출 중 오류가 발생했습니다."),
}
