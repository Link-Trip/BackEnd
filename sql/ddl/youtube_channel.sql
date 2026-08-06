-- ============================================================
-- youtube_channel
-- YouTube 채널 메타데이터. 채널 발견 / 구독자수 기준 추천에 사용.
-- ============================================================
CREATE TABLE IF NOT EXISTS `youtube_channel` (
    `id`                 VARCHAR(36)    NOT NULL,
    `channel_id`         VARCHAR(64)    NOT NULL,
    `title`              VARCHAR(500)   NOT NULL,
    `description`        TEXT           NOT NULL,
    `thumbnail_url`      VARCHAR(1000)  NOT NULL,
    `subscriber_count`   BIGINT         NOT NULL,
    `video_count`        BIGINT         NOT NULL,
    `deleted`            BIT(1)         NOT NULL,
    `created_at`         DATETIME(6)    DEFAULT NULL,
    `updated_at`         DATETIME(6)    DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- YouTube 의 외부 채널 ID 중복 저장 방지 + upsert 시 기존 row 조회 키.
    UNIQUE KEY `uk_youtube_channel_channel_id` (`channel_id`),

    -- 구독자수 기준 채널 정렬/조회 (인기 채널 추천 등).
    KEY `idx_youtube_channel_subscriber_count` (`subscriber_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
