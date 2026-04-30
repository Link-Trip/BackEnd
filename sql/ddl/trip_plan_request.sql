-- ============================================================
-- trip_plan_request
-- 멤버가 특정 영상 분석 task 에 대해 여행 계획 생성을 요청한 기록.
-- 분석 완료 시점에 미처리 (processed=false) 요청을 찾아 trip_plan 으로 전환.
-- ============================================================
CREATE TABLE IF NOT EXISTS `trip_plan_request` (
    `id`                       VARCHAR(36)  NOT NULL,
    `member_id`                VARCHAR(36)  NOT NULL,
    `video_analysis_task_id`   VARCHAR(36)  NOT NULL,
    `processed`                BIT(1)       NOT NULL,
    `deleted`                  BIT(1)       NOT NULL,
    `created_at`               DATETIME(6)  DEFAULT NULL,
    `updated_at`               DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 동일 멤버가 동일 task 에 중복 요청하지 않도록 차단 + 멤버별 요청 조회 키.
    UNIQUE KEY `uk_trip_plan_request_member_task` (`member_id`, `video_analysis_task_id`),

    -- 분석 완료된 task 에 대한 미처리 요청 batch 조회.
    -- TripPlanRequestQuerydslRepository 의 후속 처리 로직이 사용.
    KEY `idx_trip_plan_request_task_processed` (`video_analysis_task_id`, `processed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
