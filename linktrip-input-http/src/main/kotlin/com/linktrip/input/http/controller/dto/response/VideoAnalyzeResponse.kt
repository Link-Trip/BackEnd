package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.Place
import com.linktrip.application.domain.video.PlaceStatus
import com.linktrip.application.domain.video.TravelItineraryItem
import com.linktrip.application.domain.video.VideoAnalysisTask
import com.linktrip.application.domain.video.VideoAnalysisTaskStatus
import com.linktrip.application.domain.video.VideoTimeline
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "영상 분석 결과 상세 응답")
data class VideoAnalyzeResponse(
    @field:Schema(description = "영상 분석 작업 ID", example = "019d41ff-fae2-7d90-96c9-2530a95f64cf")
    val id: String,
    @field:Schema(description = "YouTube URL", example = "https://www.youtube.com/watch?v=2oLfUjAqEcM")
    val youtubeUrl: String,
    @field:Schema(description = "여행 영상 판정 결과 (false면 여행 영상이 아님)", example = "true")
    val valid: Boolean,
    @field:Schema(
        description = "분석 상태",
        example = "COMPLETED",
        allowableValues = ["PENDING", "COMPLETED", "INVALID", "FAILED"],
    )
    val status: String,
    @field:Schema(
        description = "AI가 생성한 영상 요약 (3~5문장, 한국어)",
        example = "후쿠오카 공항에 도착해 하카타 지역의 숨겨진 맛집들을 탐방하는 1박 2일 미식 여행입니다.",
        nullable = true,
    )
    val summary: String?,
    @field:Schema(description = "1인 기준 예상 최소 비용 (KRW, 국제선 항공료 제외)", example = "400000", nullable = true)
    val estimatedMinCost: Long?,
    @field:Schema(description = "1인 기준 예상 최대 비용 (KRW, 국제선 항공료 제외)", example = "600000", nullable = true)
    val estimatedMaxCost: Long?,
    @field:Schema(
        description = "비용 추정 근거. VIDEO_MENTIONED: 영상에서 직접 언급된 비용 (±10%), ITEM_ESTIMATED: 개별 항목 가격 합산 추정",
        example = "ITEM_ESTIMATED",
        allowableValues = ["VIDEO_MENTIONED", "ITEM_ESTIMATED"],
        nullable = true,
    )
    val costBasis: String?,
    @field:Schema(description = "모든 일정 아이템의 장소 검색이 완료되었는지 여부", example = "true")
    val placeEnrichmentCompleted: Boolean,
    @field:Schema(description = "영상 타임라인 목록 (주요 장면별 타임스탬프, 시간순 정렬)")
    val timelines: List<TimelineResponse>,
    @field:Schema(description = "일차별 여행 일정 아이템 목록")
    val itineraryItems: List<ScheduleItemResponse>,
) {
    @Schema(description = "영상 타임라인 항목")
    data class TimelineResponse(
        @field:Schema(description = "영상 재생 시점 (초 단위)", example = "135")
        val timestampSeconds: Int,
        @field:Schema(description = "사람이 읽을 수 있는 타임스탬프 (m:ss 또는 h:mm:ss)", example = "2:15")
        val timestamp: String,
        @field:Schema(
            description = "해당 시점으로 이동하는 YouTube 딥링크",
            example = "https://www.youtube.com/watch?v=2oLfUjAqEcM&t=135",
        )
        val timestampUrl: String,
        @field:Schema(description = "해당 장면 설명 (한국어, 30자 이내)", example = "시부야 스크램블 교차로")
        val description: String,
    ) {
        companion object {
            fun from(
                timeline: VideoTimeline,
                youtubeUrl: String,
            ): TimelineResponse =
                TimelineResponse(
                    timestampSeconds = timeline.timestampSeconds,
                    timestamp = formatTimestamp(timeline.timestampSeconds),
                    timestampUrl = "$youtubeUrl&t=${timeline.timestampSeconds}",
                    description = timeline.description,
                )

            private fun formatTimestamp(seconds: Int): String {
                val h = seconds / 3600
                val m = (seconds % 3600) / 60
                val s = seconds % 60
                return if (h > 0) {
                    "%d:%02d:%02d".format(h, m, s)
                } else {
                    "%d:%02d".format(m, s)
                }
            }
        }
    }

    @Schema(description = "여행 일정 아이템")
    data class ScheduleItemResponse(
        @field:Schema(description = "일정 아이템 ID", example = "019d4202-b486-796d-8b28-c52befa3a65b")
        val id: String,
        @field:Schema(description = "여행 일차 (1부터 시작)", example = "1")
        val day: Int,
        @field:Schema(description = "해당 일차 내 방문 순서 (1부터 시작)", example = "3")
        val order: Int,
        @field:Schema(
            description = "장소 카테고리",
            example = "EAT",
            allowableValues = [
                "EAT",
                "ATTRACTION",
                "SHOPPING",
                "TRANSPORTATION_HUB",
                "TRANSPORTATION_TRANSIT",
            ],
        )
        val category: String,
        @field:Schema(description = "장소 이름", example = "이치란 라멘")
        val name: String,
        @field:Schema(description = "장소 설명 (AI 생성)", example = "돈코츠 라멘 전문점", nullable = true)
        val description: String?,
        @field:Schema(description = "방문 팁 (AI 생성)", example = "오픈 전 줄서기 추천", nullable = true)
        val tips: String?,
        @field:Schema(description = "Google Places 장소 정보 (검색 완료 시에만 존재)", nullable = true)
        val place: PlaceResponse?,
        @field:Schema(
            description = "장소 검색 상태. FOUND: 검색 완료, PENDING: 검색 전, SEARCHING: 검색 중, NOT_FOUND: 미발견, NOT_REQUIRED: 불필요",
            example = "FOUND",
            allowableValues = ["FOUND", "PENDING", "SEARCHING", "NOT_FOUND", "NOT_REQUIRED"],
        )
        val placeStatus: String,
    )

    @Schema(description = "Google Places 장소 정보")
    data class PlaceResponse(
        @field:Schema(description = "장소 ID", example = "019d4202-c0fb-743c-ac45-415d7cda63b2")
        val id: String,
        @field:Schema(description = "장소 이름", example = "후쿠오카 공항")
        val name: String,
        @field:Schema(description = "Google Place ID", example = "ChIJrQFpQhaQQTURtx9OWEZ_5hY")
        val googlePlaceId: String,
        @field:Schema(
            description = "주소",
            example = "778-1 Shimousui, Hakata Ward, Fukuoka, 812-0003 일본",
            nullable = true,
        )
        val address: String?,
        @field:Schema(description = "위도", example = "33.5849988", nullable = true)
        val latitude: Double?,
        @field:Schema(description = "경도", example = "130.4490906", nullable = true)
        val longitude: Double?,
    ) {
        companion object {
            fun from(place: Place): PlaceResponse =
                PlaceResponse(
                    id = place.id,
                    name = place.name,
                    googlePlaceId = place.googlePlaceId,
                    address = place.address,
                    latitude = place.latitude,
                    longitude = place.longitude,
                )
        }
    }

    companion object {
        fun from(
            videoAnalysisTask: VideoAnalysisTask,
            items: List<TravelItineraryItem>,
            timelines: List<VideoTimeline>,
        ): VideoAnalyzeResponse =
            VideoAnalyzeResponse(
                id = videoAnalysisTask.id,
                youtubeUrl = videoAnalysisTask.youtubeUrl,
                valid = videoAnalysisTask.valid,
                status = videoAnalysisTask.status.name,
                summary = videoAnalysisTask.summary,
                estimatedMinCost = videoAnalysisTask.estimatedMinCost,
                estimatedMaxCost = videoAnalysisTask.estimatedMaxCost,
                costBasis = videoAnalysisTask.costBasis?.name,
                placeEnrichmentCompleted =
                    videoAnalysisTask.status == VideoAnalysisTaskStatus.COMPLETED &&
                        items.isNotEmpty() &&
                        items.all { it.isResolved() },
                timelines = timelines.map { TimelineResponse.from(it, videoAnalysisTask.youtubeUrl) },
                itineraryItems =
                    items.map { item ->
                        ScheduleItemResponse(
                            id = item.id,
                            day = item.day,
                            order = item.itemOrder,
                            category = item.category.name,
                            name = item.name,
                            description = item.description,
                            tips = item.tips,
                            place = item.place?.let { PlaceResponse.from(it) },
                            placeStatus = PlaceStatus.from(item).name,
                        )
                    },
            )
    }
}
