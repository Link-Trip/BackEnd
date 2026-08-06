-- ============================================================
-- api_call_count
-- 외부 API 별 일자별 호출 누적 카운트.
-- update-first 패턴: atomic UPDATE +1 시도, 그날 첫 호출 시에만 INSERT.
-- ============================================================
CREATE TABLE IF NOT EXISTS `api_call_count` (
    `id`          VARCHAR(36)  NOT NULL,
    `api_type`    VARCHAR(40)  NOT NULL  COMMENT 'GEMINI / YOUTUBE_DATA / GOOGLE_PLACES',
    `call_date`   DATE         NOT NULL,
    `call_count`  BIGINT       NOT NULL  DEFAULT 0,
    `deleted`     BIT(1)       NOT NULL,
    `created_at`  DATETIME(6)  DEFAULT NULL,
    `updated_at`  DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- atomic UPSERT 의 row 식별. (api_type, call_date) 조합 중복 차단.
    -- ApiCallCountQuerydslRepository.incrementCallCount 의 WHERE 조건이 사용.
    UNIQUE KEY `uk_api_call_count_type_date` (`api_type`, `call_date`),

    -- ApiCallCounterService.computeCostBreakdown 의 findAllByDate(date) 가 사용.
    -- call_date 가 leading column 이라 해당 일자 row 를 즉시 range scan.
    KEY `idx_api_call_count_date_type` (`call_date`, `api_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
