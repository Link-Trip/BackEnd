package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.UpdateTripPlanRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.TripPlanCursorResponse
import com.linktrip.input.http.controller.dto.response.TripPlanDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "TripPlan", description = "여행 계획 관리 API")
interface TripPlanDocs {
    @Operation(
        summary = "내 여행 계획 목록 조회 (커서 페이지네이션)",
        description = """
            로그인한 사용자의 여행 계획 목록을 커서 기반 페이지네이션으로 조회합니다.

            **응답 데이터:**
            - 여행 계획 요약 (제목, 일정 아이템 수, 박/일 수, 해시태그)
            - 연결된 영상 분석 ID 및 YouTube URL

            **페이지네이션:**
            - 첫 요청: cursor 미전달
            - 다음 페이지: 이전 응답의 `nextCursor` 값을 cursor 파라미터로 전달
            - `hasNext`가 false면 마지막 페이지

            **자동 생성:**
            영상 분석 완료 시 자동으로 여행 계획이 생성됩니다. 분석 요청 시점에 계정이 연결되어 있으면 즉시 생성되고, 아니면 분석 완료 후 해당 영상을 조회할 때 생성됩니다.
        """,
    )
    fun getTripPlans(
        @Parameter(hidden = true) memberId: String,
        @Parameter(description = "페이지네이션 커서 (ISO-8601 형식)", example = "2025-01-15T10:30:00")
        cursor: String?,
    ): ApiResponse<TripPlanCursorResponse>

    @Operation(
        summary = "여행 계획 상세 조회",
        description = """
            특정 여행 계획의 상세 정보를 조회합니다.

            **응답 데이터:**
            - 여행 계획 메타 정보 (제목, 생성일, 수정일)
            - 일정 아이템 목록 (일차, 순서, 장소명, 카테고리, 설명, 팁)
            - 각 아이템에 연결된 Google Places 장소 정보 (주소, 좌표)

            **category 필드:**
            - `EAT`: 음식점, 카페, 길거리 음식
            - `ATTRACTION`: 관광지, 박물관, 사찰, 공원
            - `SHOPPING`: 쇼핑몰, 면세점, 기념품점
            - `TRANSPORTATION_HUB`: 공항, 기차역, 버스터미널
            - `TRANSPORTATION_TRANSIT`: 지하철, 택시, 버스 이동
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "여행 계획 상세 조회 성공",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 여행 계획이 아닌 경우",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"FORBIDDEN_TRIP_PLAN",""" +
                                        """"message":"해당 여행 계획에 접근할 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 여행 계획",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"NOT_FOUND_TRIP_PLAN",""" +
                                        """"message":"여행 계획을 찾을 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getTripPlanDetail(
        @Parameter(hidden = true) memberId: String,
        @Parameter(description = "여행 계획 ID", example = "019d4200-a1b2-7c3d-8e4f-567890abcdef")
        tripPlanId: String,
    ): ApiResponse<TripPlanDetailResponse>

    @Operation(
        summary = "여행 계획 수정",
        description = """
            여행 계획의 제목 또는 일정 아이템의 일차/순서를 수정합니다.

            **수정 가능 항목:**
            - `title`: 여행 계획 제목 변경 (null이면 변경하지 않음)
            - `items`: 일정 아이템의 일차(day)와 순서(itemOrder) 변경 (null이면 변경하지 않음)

            **주의:** items 배열에 포함된 아이템만 수정됩니다. 포함되지 않은 아이템은 기존 값이 유지됩니다.
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "여행 계획 수정 성공 (수정된 상세 정보 반환)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 여행 계획이 아닌 경우",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"FORBIDDEN_TRIP_PLAN",""" +
                                        """"message":"해당 여행 계획에 접근할 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 여행 계획 또는 아이템",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"NOT_FOUND_TRIP_PLAN",""" +
                                        """"message":"여행 계획을 찾을 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun updateTripPlan(
        @Parameter(hidden = true) memberId: String,
        @Parameter(description = "여행 계획 ID", example = "019d4200-a1b2-7c3d-8e4f-567890abcdef")
        tripPlanId: String,
        request: UpdateTripPlanRequest,
    ): ApiResponse<TripPlanDetailResponse>

    @Operation(
        summary = "여행 계획 삭제",
        description = """
            특정 여행 계획을 소프트 삭제합니다. 삭제 후 목록에서 더 이상 조회되지 않습니다.

            **멱등성:**
            - 현재 이 API에는 `Idempotency-Key` 기반 멱등성 처리가 적용되어 있지 않습니다.
            - 앱스토어 배포 이후 컨트롤러에 멱등성 처리를 적용할 예정입니다.
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "본인의 여행 계획이 아닌 경우",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"FORBIDDEN_TRIP_PLAN",""" +
                                        """"message":"해당 여행 계획에 접근할 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 여행 계획",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"NOT_FOUND_TRIP_PLAN",""" +
                                        """"message":"여행 계획을 찾을 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun deleteTripPlan(
        @Parameter(hidden = true) memberId: String,
        @Parameter(description = "여행 계획 ID", example = "019d4200-a1b2-7c3d-8e4f-567890abcdef")
        tripPlanId: String,
    ): ApiResponse<Unit>
}
