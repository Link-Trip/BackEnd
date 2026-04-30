-- ============================================================
-- video_analysis_task
-- YouTube 영상에 대한 분석 task. 큐 컨슈머가 PENDING → PROCESSING → COMPLETED/FAILED 로 전이.
-- source 는 task 생성 트리거 (USER / BATCH) 의 audit 기록 (immutable).
-- ============================================================
CREATE TABLE IF NOT EXISTS `video_analysis_task` (
    `id`                   VARCHAR(36)   NOT NULL,
    `youtube_url`          VARCHAR(512)  NOT NULL,
    `valid`                BIT(1)        NOT NULL,
    `status`               VARCHAR(20)   NOT NULL  COMMENT 'PENDING / PROCESSING / COMPLETED / FAILED / INVALID',
    `source`               VARCHAR(20)   NOT NULL  COMMENT 'USER / BATCH (audit, immutable)',
    `estimated_min_cost`   BIGINT        DEFAULT NULL,
    `estimated_max_cost`   BIGINT        DEFAULT NULL,
    `summary`              TEXT          DEFAULT NULL,
    `cost_basis`           VARCHAR(20)   DEFAULT NULL  COMMENT 'ITEM_ESTIMATED / VIDEO_MENTIONED',
    `destination`          VARCHAR(100)  DEFAULT NULL,
    `deleted`              BIT(1)        NOT NULL,
    `created_at`           DATETIME(6)   DEFAULT NULL,
    `updated_at`           DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 동일 YouTube URL 에 대한 분석 task 중복 생성 방지.
    -- 신규 분석 요청 시 기존 task 존재 여부 조회 키로도 사용.
    UNIQUE KEY `uk_video_analysis_task_youtube_url` (`youtube_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
