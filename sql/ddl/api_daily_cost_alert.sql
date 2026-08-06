-- ============================================================
-- api_daily_cost_alert
-- 일자별 마지막으로 발송된 비용 알림 임계값 (KRW) 추적.
-- 같은 임계값 구간에 대해 알림이 두 번 나가지 않도록 함.
-- ============================================================
CREATE TABLE IF NOT EXISTS `api_daily_cost_alert` (
    `id`                       VARCHAR(36) NOT NULL,
    `alert_date`               DATE        NOT NULL,
    `last_sent_threshold_krw`  BIGINT      NOT NULL,
    `deleted`                  BIT(1)      NOT NULL,
    `created_at`               DATETIME(6) DEFAULT NULL,
    `updated_at`               DATETIME(6) DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 1일 1 row 보장.
    -- ApiDailyCostAlertJpaRepository.findByAlertDate / 어댑터의 select-then-update 에서 사용.
    UNIQUE KEY `uk_api_daily_cost_alert_date` (`alert_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
