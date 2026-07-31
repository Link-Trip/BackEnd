package com.linktrip.input.http.controller.docs

import com.linktrip.input.http.controller.dto.request.VideoAnalyzeRequest
import com.linktrip.input.http.controller.dto.response.ApiResponse
import com.linktrip.input.http.controller.dto.response.DiscoverChannelResponses
import com.linktrip.input.http.controller.dto.response.DiscoverCountryResponses
import com.linktrip.input.http.controller.dto.response.DiscoverVideoCursorResponse
import com.linktrip.input.http.controller.dto.response.DiscoverVideoResponses
import com.linktrip.input.http.controller.dto.response.VideoAnalyzeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Video", description = "영상 분석 및 탐색 API")
interface VideoDocs {
    @Operation(
        summary = "YouTube 영상 분석 요청",
        description = """
            YouTube URL을 전달하면 AI(Gemini)가 영상을 분석하여 여행 일정, 타임라인, 요약 등을 추출합니다.

            **처리 흐름:**
            1. 최초 요청 → 분석 작업 생성 후 202 Accepted 반환 (비동기 분석 시작, schedule 폴링 필요)
            2. 동일 URL 재요청
                - 분석 완료된 영상이면 → 200 OK + 결과 데이터 인라인 반환 (추가 폴링 불필요)
                - 진행 중이면 → 202 Accepted + 현재 상태만 반환
            3. 이전 분석 실패(FAILED) URL 재요청 → 재분석 시작 (202 Accepted)

            **상태값 / HTTP 상태 코드:**
            - `PENDING` → 202 Accepted: 분석 진행 중. 결과 필드는 비어있음. schedule API 폴링 필요
            - `PROCESSING` → 202 Accepted: 컨슈머가 처리 중. 결과 필드는 비어있음
            - `COMPLETED` → 200 OK: 분석 완료. 응답에 모든 결과 데이터 포함 (추가 호출 불필요)
            - `INVALID` → 200 OK: 여행 영상이 아닌 것으로 판정. 결과 필드는 비어있음
            - `FAILED` → 202 Accepted: 직전 실패 후 재분석 시작 (status 는 PENDING 으로 응답)

            **응답 shape:**
            schedule API(GET /video/schedule/{id}) 와 동일한 [VideoAnalyzeResponse]. status 가 COMPLETED 일 때만 결과 필드가 채워짐.

            **멱등성:**
            - `GET`을 제외한 모든 API는 `Idempotency-Key` 헤더가 필수입니다.
            - 동일한 `Idempotency-Key`로 처리 중인 요청이 있으면 409를 반환합니다.
        """,
        parameters = [
            Parameter(
                name = "Idempotency-Key",
                description = "멱등성 키 (UUID v4 권장, non-GET 요청 필수)",
                `in` = ParameterIn.HEADER,
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "이미 분석 완료된 영상 (COMPLETED/INVALID) — 결과 인라인 반환",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "202",
                description = "분석 진행 중 (PENDING/PROCESSING) — 폴링 필요",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 YouTube URL 형식, 자막 없는 영상 또는 Idempotency-Key 헤더 누락",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "멱등성 키 누락",
                                value =
                                    """{"code":"BAD_REQUEST_MISSING_IDEMPOTENCY_KEY",""" +
                                        """"message":"Idempotency-Key 헤더는 필수입니다."}""",
                            ),
                            ExampleObject(
                                name = "잘못된 URL",
                                value =
                                    """{"code":"BAD_REQUEST_YOUTUBE_URL",""" +
                                        """"message":"유효하지 않은 YouTube URL입니다."}""",
                            ),
                            ExampleObject(
                                name = "자막 없는 영상",
                                value =
                                    """{"code":"BAD_REQUEST_VIDEO",""" +
                                        """"message":"자막을 추출할 수 없는 영상입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "동일한 멱등성 키의 요청이 이미 처리 중인 경우",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"DUPLICATE_REQUEST",""" +
                                        """"message":"이미 요청한 값입니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "429",
                description = "API 요청 횟수 초과",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"TOO_MANY_REQUESTS",""" +
                                        """"message":"요청이 너무 많습니다. 잠시 후 다시 시도해주세요."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun analyzeVideo(
        @Parameter(hidden = true) memberId: String,
        request: VideoAnalyzeRequest,
    ): ApiResponse<VideoAnalyzeResponse>

    @Operation(
        summary = "영상 분석 결과 상세 조회",
        description = """
            분석이 완료된 영상의 상세 정보를 조회합니다.

            **응답 포함 데이터:**
            - 영상 메타 정보 (제목, 요약, 예상 비용, 해시태그)
            - 타임라인 (영상 주요 장면별 타임스탬프 + YouTube 딥링크)
            - 일정 아이템 (일차별 장소 목록 + Google Places 정보)

            **상태별 HTTP 코드:**
            - `PENDING` → 202 Accepted (분석 진행 중, 폴링 필요)
            - 그 외 → 200 OK

            **costBasis 필드:**
            - `VIDEO_MENTIONED`: 영상에서 직접 언급된 비용 기반 (±10% 범위)
            - `ITEM_ESTIMATED`: 개별 항목 가격 합산 추정치
            - `null`: 비용 추정 불가

            **placeStatus 필드:**
            - `FOUND`: Google Places에서 장소 검색 완료
            - `PENDING`: 아직 장소 검색 전
            - `SEARCHING`: 장소 검색 진행 중
            - `NOT_FOUND`: 장소 검색 결과 없음
            - `NOT_REQUIRED`: 장소 검색 불필요 (교통수단 등)

            **category 필드:**
            - `EAT`: 음식점, 카페, 길거리 음식
            - `ATTRACTION`: 관광지, 박물관, 사찰, 공원
            - `SHOPPING`: 쇼핑몰, 면세점, 기념품점
            - `TRANSPORTATION_HUB`: 공항, 기차역, 버스터미널
            - `TRANSPORTATION_TRANSIT`: 지하철, 택시, 버스 이동

            **placeEnrichmentCompleted 필드:**
            - `true`: 모든 일정 아이템의 장소 검색이 완료됨
            - `false`: 아직 장소 검색이 완료되지 않은 아이템이 있음

            **timeline 필드:**
            - `timestampSeconds`: 영상 재생 시작 시점 (초 단위)
            - `timestamp`: 사람이 읽을 수 있는 형식 (예: "2:15", "1:03:20")
            - `timestampUrl`: 해당 시점으로 이동하는 YouTube 딥링크
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "분석 완료된 영상 상세 정보",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "202",
                description = "분석 진행 중 (폴링 필요)",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 영상 분석 ID",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"NOT_FOUND_VIDEO_ANALYSIS_TASK",""" +
                                        """"message":"영상 분석 결과를 찾을 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getVideoSchedule(
        @Parameter(hidden = true) memberId: String,
        @Parameter(description = "영상 분석 작업 ID", example = "019d41ff-fae2-7d90-96c9-2530a95f64cf")
        videoAnalysisTaskId: String,
    ): ApiResponse<VideoAnalyzeResponse>

    @Operation(
        summary = "탐색 영상 카테고리별 조회",
        description = """
            나라별 또는 지역별로 큐레이션된 여행 영상 목록을 조회합니다.

            **파라미터 규칙:**
            - `country`와 `region` 중 하나만 사용 가능 (둘 다 전달 시 400 에러)
            - 둘 다 미전달 시 전체 영상 목록 반환
        """,
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "영상 목록 조회 성공",
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "country와 region 동시 전달",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value =
                                    """{"code":"BAD_REQUEST_DISCOVER_QUERY",""" +
                                        """"message":"country와 region은 동시에 사용할 수 없습니다."}""",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getVideos(
        @Parameter(description = "국가 필터 (예: 일본, 태국)", example = "일본")
        country: String?,
        @Parameter(description = "지역 필터 (예: 동남아시아, 유럽)", example = "동남아시아")
        region: String?,
    ): ApiResponse<DiscoverVideoResponses>

    @Operation(
        summary = "탐색 채널 목록 조회",
        description = "큐레이션된 여행 유튜브 채널 목록과 각 채널의 최신 영상을 조회합니다.",
    )
    fun getChannels(): ApiResponse<DiscoverChannelResponses>

    @Operation(
        summary = "대표 여행지(나라) TOP 10 조회",
        description = """
            사용자들이 생성한 일정 수 기준 상위 10개 나라를 조회합니다.
            일정이 많은 순으로 정렬되어 내려가며, 클라이언트는 이 중 랜덤으로 노출할 수 있습니다.
        """,
    )
    fun getTopCountries(): ApiResponse<DiscoverCountryResponses>

    @Operation(
        summary = "테마별 영상 목록 조회 (커서 페이지네이션)",
        description = """
            테마별로 여행 영상을 커서 기반 페이지네이션으로 조회합니다.

            **페이지네이션:**
            - 첫 요청: cursor 미전달
            - 다음 페이지: 이전 응답의 `nextCursor` 값을 cursor 파라미터로 전달
            - `hasNext`가 false면 마지막 페이지
        """,
    )
    fun getVideosByTheme(
        @Parameter(description = "테마 (예: 맛집여행, 힐링여행, 액티비티)", example = "맛집여행")
        theme: String,
        @Parameter(description = "페이지네이션 커서 (ISO-8601 형식)", example = "2025-01-15T10:30:00")
        cursor: String?,
    ): ApiResponse<DiscoverVideoCursorResponse>
}
