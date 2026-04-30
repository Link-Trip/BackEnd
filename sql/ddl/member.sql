-- ============================================================
-- member
-- 사용자 계정.
-- ============================================================
CREATE TABLE IF NOT EXISTS `member` (
    `id`             VARCHAR(36)   NOT NULL,
    `serial_number`  VARCHAR(255)  NOT NULL,
    `deleted`        BIT(1)        NOT NULL,
    `created_at`     DATETIME(6)   DEFAULT NULL,
    `updated_at`     DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 외부 식별자 (디바이스 시리얼 등) 의 중복 가입 방지 + 로그인/조회 키.
    UNIQUE KEY `uk_member_serial_number` (`serial_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
