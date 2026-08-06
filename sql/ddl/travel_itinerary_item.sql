-- ============================================================
-- travel_itinerary_item
-- 한 영상 분석 task 안의 일자/순서별 여행 아이템 (식당/관광지/이동 등).
-- ============================================================
CREATE TABLE IF NOT EXISTS `travel_itinerary_item` (
    `id`                       VARCHAR(36)   NOT NULL,
    `video_analysis_task_id`   VARCHAR(36)   NOT NULL,
    `day`                      INT           NOT NULL,
    `item_order`               INT           NOT NULL,
    `category`                 VARCHAR(30)   NOT NULL  COMMENT 'ATTRACTION / EAT / SHOPPING / TRANSPORTATION_HUB / TRANSPORTATION_TRANSIT',
    `name`                     VARCHAR(255)  NOT NULL,
    `description`              VARCHAR(500)  DEFAULT NULL,
    `tips`                     VARCHAR(500)  DEFAULT NULL,
    `place_id`                 VARCHAR(36)   DEFAULT NULL,
    `place_search_count`       INT           NOT NULL,
    `deleted`                  BIT(1)        NOT NULL,
    `created_at`               DATETIME(6)   DEFAULT NULL,
    `updated_at`               DATETIME(6)   DEFAULT NULL,

    PRIMARY KEY (`id`),

    -- 한 task 의 미삭제 아이템을 day → item_order 순으로 조회.
    -- TravelItineraryItemQuerydslRepository / TripPlanQuerydslRepository 의
    -- itinerary 조회 / 정렬 쿼리가 사용. deleted 를 키 안에 둬서 soft delete 항목 자동 제외.
    KEY `idx_itinerary_item_task_deleted_day_order` (`video_analysis_task_id`, `deleted`, `day`, `item_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
