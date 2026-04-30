-- ============================================================
-- place
-- Google Places 로 보강된 장소 정보. 한 번 INSERT 후 immutable
-- (PlaceEntity 의 @PreUpdate / @PreRemove / softDelete 모두 throw 처리).
-- ============================================================
CREATE TABLE IF NOT EXISTS `place` (
    `id`               VARCHAR(36)   NOT NULL,
    `name`             VARCHAR(255)  NOT NULL,
    `google_place_id`  VARCHAR(255)  NOT NULL,
    `address`          VARCHAR(500)  DEFAULT NULL,
    `latitude`         DOUBLE        DEFAULT NULL,
    `longitude`        DOUBLE        DEFAULT NULL,
    `deleted`          BIT(1)        NOT NULL,
    `created_at`       DATETIME(6)   DEFAULT NULL,
    `updated_at`       DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- Google Places 의 동일 장소를 여러 row 로 저장하지 않도록 차단.
    -- 신규 enrich 결과가 들어올 때 기존 row 가 있는지 검증하는 키.
    UNIQUE KEY `uk_place_google_place_id` (`google_place_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
