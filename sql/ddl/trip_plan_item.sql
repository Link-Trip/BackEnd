-- ============================================================
-- trip_plan_item
-- 여행 계획에 속한 일자/순서별 아이템.
-- travel_itinerary_item 과 1:1 로 연결되며, 일자/순서는 trip_plan 안에서 재정렬 가능.
-- ============================================================
CREATE TABLE IF NOT EXISTS `trip_plan_item` (
    `id`                         VARCHAR(36)  NOT NULL,
    `trip_plan_id`               VARCHAR(36)  NOT NULL,
    `travel_itinerary_item_id`   VARCHAR(36)  NOT NULL,
    `day`                        INT          NOT NULL,
    `item_order`                 INT          NOT NULL,
    `deleted`                    BIT(1)       NOT NULL,
    `created_at`                 DATETIME(6)  DEFAULT NULL,
    `updated_at`                 DATETIME(6)  DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 한 trip_plan 의 미삭제 아이템을 day → item_order 순으로 조회.
    -- TripPlanItemQuerydslRepository / TripPlanQuerydslRepository 의 plan 상세 조회가 사용.
    KEY `idx_trip_plan_item_plan_deleted_day_order` (`trip_plan_id`, `deleted`, `day`, `item_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
