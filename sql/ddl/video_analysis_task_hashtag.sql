-- ============================================================
-- video_analysis_task_hashtag
-- video_analysis_task 와 hashtag 의 다대다 매핑.
-- ============================================================
CREATE TABLE IF NOT EXISTS `video_analysis_task_hashtag` (
    `id`                       VARCHAR(36)  NOT NULL,
    `video_analysis_task_id`   VARCHAR(36)  NOT NULL,
    `hashtag_id`               VARCHAR(36)  NOT NULL,
    `deleted`                  BIT(1)       NOT NULL,
    `created_at`               DATETIME(6)  DEFAULT NULL,
    `updated_at`               DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 동일 (task, hashtag) 매핑 중복 INSERT 차단.
    -- task 별 hashtag 조회 시 leading column 이 task_id 라 그대로 활용.
    UNIQUE KEY `uk_task_hashtag` (`video_analysis_task_id`, `hashtag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
