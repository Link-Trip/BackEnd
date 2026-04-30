-- ============================================================
-- video_timeline
-- 영상 분석 결과의 timestamp 별 설명 (목차).
-- 한 task 안에서 timestamp_seconds 오름차순으로 노출.
-- ============================================================
CREATE TABLE IF NOT EXISTS `video_timeline` (
    `id`                       VARCHAR(36)   NOT NULL,
    `video_analysis_task_id`   VARCHAR(36)   NOT NULL,
    `timestamp_seconds`        INT           NOT NULL,
    `description`              VARCHAR(255)  NOT NULL,
    `deleted`                  BIT(1)        NOT NULL,
    `created_at`               DATETIME(6)   DEFAULT NULL,
    `updated_at`               DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 한 task 의 미삭제 timeline 을 시간 순으로 조회.
    -- VideoTimelineQuerydslRepository.findByVideoAnalysisTaskId 가 사용.
    KEY `idx_video_timeline_task_deleted_timestamp` (`video_analysis_task_id`, `deleted`, `timestamp_seconds`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
