package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.port.input.TripPlanDetail
import com.linktrip.application.port.input.TripPlanSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "여행 계획 요약 정보")
data class TripPlanSummaryResponse(
    @field:Schema(description = "여행 계획 ID", example = "019d4200-a1b2-7c3d-8e4f-567890abcdef")
    val id: String,
    @field:Schema(description = "여행 계획 제목", example = "후쿠오카 1박 2일 여행")
    val title: String,
    @field:Schema(description = "연결된 영상 분석 작업 ID", example = "019d41ff-fae2-7d90-96c9-2530a95f64cf")
    val videoAnalysisTaskId: String,
    @field:Schema(description = "연결된 YouTube URL", example = "https://www.youtube.com/watch?v=2oLfUjAqEcM")
    val youtubeUrl: String,
    @field:Schema(description = "일정 아이템 총 개수", example = "15")
    val itemCount: Int,
    @field:Schema(description = "숙박 수 (박)", example = "1")
    val nights: Int,
    @field:Schema(description = "여행 일수 (일)", example = "2")
    val days: Int,
    @field:Schema(description = "해시태그 목록", example = "[\"맛집여행\", \"가성비여행\"]")
    val hashtags: Set<String>,
    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,
    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(summary: TripPlanSummary): TripPlanSummaryResponse =
            TripPlanSummaryResponse(
                id = summary.tripPlan.id,
                title = summary.tripPlan.title,
                videoAnalysisTaskId = summary.tripPlan.videoAnalysisTaskId,
                youtubeUrl = summary.youtubeUrl,
                itemCount = summary.itemCount,
                nights = summary.nights,
                days = summary.days,
                hashtags = summary.hashtags,
                createdAt = summary.tripPlan.createdAt,
                updatedAt = summary.tripPlan.updatedAt,
            )
    }
}

@Schema(description = "여행 계획 목록 커서 페이지네이션 응답")
data class TripPlanCursorResponse(
    @field:Schema(description = "여행 계획 요약 목록")
    val tripPlans: List<TripPlanSummaryResponse>,
    @field:Schema(description = "다음 페이지 커서 (마지막 페이지면 null)", example = "2025-01-15T10:30:00", nullable = true)
    val nextCursor: String?,
    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
) {
    companion object {
        fun from(page: CursorPage<TripPlanSummary>): TripPlanCursorResponse =
            TripPlanCursorResponse(
                tripPlans = page.items.map { TripPlanSummaryResponse.from(it) },
                nextCursor = page.nextCursor,
                hasNext = page.hasNext,
            )
    }
}

@Schema(description = "여행 계획 상세 응답")
data class TripPlanDetailResponse(
    @field:Schema(description = "여행 계획 ID", example = "019d4200-a1b2-7c3d-8e4f-567890abcdef")
    val id: String,
    @field:Schema(description = "여행 계획 제목", example = "후쿠오카 1박 2일 여행")
    val title: String,
    @field:Schema(description = "연결된 영상 분석 작업 ID", example = "019d41ff-fae2-7d90-96c9-2530a95f64cf")
    val videoAnalysisTaskId: String,
    @field:Schema(description = "일정 아이템 상세 목록")
    val items: List<TripPlanItemDetailResponse>,
    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,
    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime,
) {
    @Schema(description = "여행 계획 일정 아이템 상세")
    data class TripPlanItemDetailResponse(
        @field:Schema(description = "여행 계획 아이템 ID", example = "019d4205-1234-7a5b-8c6d-789012345678")
        val id: String,
        @field:Schema(description = "원본 일정 아이템 ID (영상 분석 결과의 아이템 참조)", example = "019d4202-b486-796d-8b28-c52befa3a65b")
        val travelItineraryItemId: String,
        @field:Schema(description = "여행 일차 (1부터 시작)", example = "1")
        val day: Int,
        @field:Schema(description = "해당 일차 내 방문 순서 (1부터 시작)", example = "3")
        val itemOrder: Int,
        @field:Schema(description = "장소 이름", example = "이치란 라멘")
        val name: String,
        @field:Schema(
            description = "장소 카테고리",
            example = "EAT",
            allowableValues = ["EAT", "ATTRACTION", "SHOPPING", "TRANSPORTATION_HUB", "TRANSPORTATION_TRANSIT"],
        )
        val category: String,
        @field:Schema(description = "장소 설명", example = "돈코츠 라멘 전문점", nullable = true)
        val description: String?,
        @field:Schema(description = "방문 팁", example = "오픈 전 줄서기 추천", nullable = true)
        val tips: String?,
        @field:Schema(description = "Google Places 장소 정보", nullable = true)
        val place: VideoAnalyzeResponse.PlaceResponse?,
    )

    companion object {
        fun from(detail: TripPlanDetail): TripPlanDetailResponse =
            TripPlanDetailResponse(
                id = detail.tripPlan.id,
                title = detail.tripPlan.title,
                videoAnalysisTaskId = detail.tripPlan.videoAnalysisTaskId,
                items =
                    detail.items.map { itemDetail ->
                        TripPlanItemDetailResponse(
                            id = itemDetail.tripPlanItem.id,
                            travelItineraryItemId = itemDetail.tripPlanItem.travelItineraryItemId,
                            day = itemDetail.tripPlanItem.day,
                            itemOrder = itemDetail.tripPlanItem.itemOrder,
                            name = itemDetail.travelItineraryItem.name,
                            category = itemDetail.travelItineraryItem.category.name,
                            description = itemDetail.travelItineraryItem.description,
                            tips = itemDetail.travelItineraryItem.tips,
                            place =
                                itemDetail.travelItineraryItem.place?.let {
                                    VideoAnalyzeResponse.PlaceResponse.from(it)
                                },
                        )
                    },
                createdAt = detail.tripPlan.createdAt,
                updatedAt = detail.tripPlan.updatedAt,
            )
    }
}
