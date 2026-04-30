-- ============================================================
-- youtube_recent_video
-- 채널별 최근 업로드 영상 캐시 (BaseTimeEntity 미상속 — deleted/created_at/updated_at 없음).
-- 채널 상세 조회 시 최근 영상 N 개를 즉시 반환하는 용도.
-- ============================================================
CREATE TABLE IF NOT EXISTS `youtube_recent_video` (
    `id`             VARCHAR(36)    NOT NULL,
    `channel_id`     VARCHAR(64)    NOT NULL,
    `video_id`       VARCHAR(64)    NOT NULL,
    `title`          VARCHAR(500)   NOT NULL,
    `thumbnail_url`  VARCHAR(1000)  NOT NULL,
    `published_at`   VARCHAR(64)    NOT NULL,

    PRIMARY KEY (`id`),

    -- 채널별 최근 영상 조회 시 leading column.
    -- YouTubeRecentVideoQuerydslRepository 의 채널 별 영상 일괄 조회가 사용.
    KEY `idx_youtube_recent_video_channel_id` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
