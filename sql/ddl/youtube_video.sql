-- ============================================================
-- youtube_video
-- YouTube 영상 메타데이터. 발견 / 추천 / 분석 후보 풀에 사용.
-- ============================================================
CREATE TABLE IF NOT EXISTS `youtube_video` (
    `id`              VARCHAR(36)    NOT NULL,
    `video_id`        VARCHAR(32)    NOT NULL,
    `title`           VARCHAR(500)   NOT NULL,
    `description`     TEXT           NOT NULL,
    `thumbnail_url`   VARCHAR(1000)  NOT NULL,
    `channel_id`      VARCHAR(64)    NOT NULL,
    `channel_title`   VARCHAR(500)   NOT NULL,
    `view_count`      BIGINT         NOT NULL,
    `like_count`      BIGINT         NOT NULL,
    `duration`        VARCHAR(32)    NOT NULL,
    `published_at`    VARCHAR(64)    NOT NULL,
    `region`          VARCHAR(32)    NOT NULL,
    `country`         VARCHAR(64)    NOT NULL,
    `city`            VARCHAR(64)    DEFAULT NULL,
    `theme`           VARCHAR(32)    DEFAULT NULL,
    `deleted`         BIT(1)         NOT NULL,
    `created_at`      DATETIME(6)    DEFAULT NULL,
    `updated_at`      DATETIME(6)    DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- YouTube 의 외부 영상 ID 중복 저장 방지 + upsert 시 기존 row 조회 키.
    UNIQUE KEY `uk_youtube_video_video_id` (`video_id`),

    -- 국가별 인기 영상 조회 (조회수 정렬). 추천/발견 화면이 사용.
    KEY `idx_youtube_video_country_view` (`country`, `view_count`),

    -- 지역별 인기 영상 조회 (조회수 정렬).
    KEY `idx_youtube_video_region_view` (`region`, `view_count`),

    -- 테마별 최신 영상 조회. YouTubeVideoQuerydslRepository.findAllByTheme / cursor 페이지네이션이 사용.
    KEY `idx_youtube_video_theme_created` (`theme`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
