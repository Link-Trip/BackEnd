-- ============================================================
-- trip_plan
-- 멤버가 특정 영상 분석 결과로부터 생성한 여행 계획.
-- ============================================================
CREATE TABLE IF NOT EXISTS `trip_plan` (
    `id`                       VARCHAR(36)   NOT NULL,
    `member_id`                VARCHAR(36)   NOT NULL,
    `video_analysis_task_id`   VARCHAR(36)   NOT NULL,
    `title`                    VARCHAR(255)  NOT NULL,
    `deleted`                  BIT(1)        NOT NULL,
    `created_at`               DATETIME(6)   DEFAULT NULL,
    `updated_at`               DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 동일 멤버가 동일 영상에 대해 여행 계획을 두 번 생성하지 않도록 차단.
    -- 멤버의 task 별 plan 존재 여부 조회 키로도 사용.
    UNIQUE KEY `uk_trip_plan_member_task` (`member_id`, `video_analysis_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
