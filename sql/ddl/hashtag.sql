-- ============================================================
-- hashtag
-- 영상 분석 결과의 해시태그 마스터 테이블.
-- video_analysis_task_hashtag 가 (task, hashtag) 매핑을 담당.
-- ============================================================
CREATE TABLE IF NOT EXISTS `hashtag` (
    `id`          VARCHAR(36)  NOT NULL,
    `name`        VARCHAR(50)  NOT NULL,
    `deleted`     BIT(1)       NOT NULL,
    `created_at`  DATETIME(6)  DEFAULT NULL,
    `updated_at`  DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 동일 이름 해시태그 중복 INSERT 방지. 신규 해시태그 등록 시 존재 여부 조회에도 사용.
    UNIQUE KEY `uk_hashtag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
