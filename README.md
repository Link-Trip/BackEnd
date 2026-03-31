# LinkTrip Backend

유튜브 여행 영상을 AI로 분석하여 일정표를 자동 생성하고, 인기 여행 영상/크리에이터를 수집하여 제공하는 플랫폼입니다.

## Tech Stack

| 분류 | 기술 |
|------|------|
| Language | Kotlin 1.9.25, JDK 21 |
| Framework | Spring Boot 3.4, Spring Batch, Spring Security |
| ORM / Query | JPA, QueryDSL |
| Database | MySQL |
| Cache | Caffeine Cache |
| Infra | AWS EC2, Docker |
| External API | YouTube Data v3, Gemini 2.5 Flash, Google Places API, Discord Webhook |

---

## 모듈 구조

```
linktrip-bootstrap                 ← Spring Boot 진입점, DI 조립
│
├── linktrip-input-http            ← REST API Controller
├── linktrip-input-batch           ← Spring Batch Job & Scheduler
│
├── linktrip-application           ← 핵심 비즈니스 로직 (Port & Service)
│   ├── domain/                    ← 도메인 서비스
│   ├── port/input/                ← UseCase 인터페이스
│   └── port/output/               ← Output Port 인터페이스
│
├── linktrip-output-http           ← 외부 API 어댑터 (YouTube, Gemini, Places, Discord)
├── linktrip-output-cache          ← Caffeine 캐시 데코레이터 어댑터
├── linktrip-output-persistence    ← MySQL JPA + QueryDSL 어댑터
└── linktrip-common                ← 공통 예외, 이벤트, 설정
```

---

## 아키텍처

### 의존성 방향

```
                         ┌──────────────────────┐
                         │      bootstrap       │
                         │  (모든 모듈 참조,      │
                         │   DI 조립 전용)       │
                         └──────────┬───────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
 ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
 │  input-http  │          │ input-batch  │          │    common    │
 └──────┬───────┘          └──────┬───────┘          └──────────────┘
        │                         │
        │  UseCase Port           │  UseCase Port
        ▼                         ▼
 ┌─────────────────────────────────────────────────────────────┐
 │                       application                           │
 │                                                             │
 │  Domain Service ──→ Output Port Interface                   │
 │                                                             │
 └─────────────────────────────┬───────────────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │ output-cache │ │  output-     │ │  output-http │
     │  (Caffeine)  │ │  persistence │ │  (YouTube,   │
     │              │ │  (MySQL/JPA) │ │   Gemini,    │
     │  @Primary    │ │  @Qualifier  │ │   Places,    │
     └──────┬───────┘ └──────────────┘ │   Discord)   │
            │               ↑          └──────────────┘
            │   delegate    │
            └───────────────┘
             Port 인터페이스 타입으로 위임
```

모든 모듈은 `application`의 Port 인터페이스에만 의존합니다. `output-cache`와 `output-persistence`는 서로의 존재를 모릅니다.

### 캐시-영속성 분리 (데코레이터 패턴)

```
Application (Port 인터페이스 정의)
      ↑                    ↑
      │                    │
output-cache            output-persistence
(@Primary)              (@Qualifier)
CachingAdapter          PersistenceAdapter
      │                    ↑
      └── delegate ────────┘
```

- `output-cache`는 동일한 Port 인터페이스를 구현하며, DB 어댑터를 delegate로 래핑
- `@Primary`로 캐시 어댑터가 우선 주입, 내부에서 `@Qualifier`로 DB 어댑터 참조
- 캐시 제거/교체 시 `application` 코드 수정 불필요

### 캐싱 전략

```
Caffeine Cache (TTL 7시간, 최대 100건)

배치 주기(6시간) < TTL(7시간) → 배치 간 캐시 미스 방지
배치 저장 시 @CacheEvict → 전체 무효화 → 다음 요청 시 DB 재조회 (Cache-Aside)
```

---

## 영상 분석 파이프라인

```
POST /video/analyze { youtubeUrl }
  │
  ▼
┌────────────────────────────────────────────────┐
│             VideoAnalyzeService                │
│                                                │
│  URL 정규화 → 중복 확인 → Task 생성 (PENDING)     │
│  → 요청 대기열 등록 (memberId)                   │
│  → Event 발행 → 202 Accepted 즉시 반환           │
└─────────────────────┬──────────────────────────┘
                      │ @Async + @TransactionalEventListener
                      ▼
┌────────────────────────────────────────────────┐
│          VideoAnalyzeEventListener             │
│                                                │
│  Gemini 2.5 Flash 영상 분석                     │
│       │                                        │
│  유효한 여행 영상?                               │
│   ├── No → INVALID                             │
│   ▼ Yes                                        │
│  일정 저장 (EAT / ATTRACTION / SHOPPING / etc.) │
│       │                                        │
│  대기열 조회 → 각 요청자별 TripPlan 생성           │
│       │                                       │
│  Google Places API 좌표 매핑 (병렬)              │
│       │                                        │
│  완료 알림 발송                                  │
└────────────────────────────────────────────────┘
                      │
                      ▼
GET /video/{id}/schedule → 일정표 + 장소 좌표 반환
```

### 상태 전이

```
PENDING ──→ COMPLETED (분석 성공)
   ├──→ INVALID   (여행 영상 아님)
   └──→ FAILED    (AI 분석 오류) ──→ 재요청 시 PENDING 복원
```

---


### 인기 여행 영상

```
YouTubeCollectJob
  │
  ├── 24개 키워드 중 5개 랜덤 선택
  ├── YouTube Search API → 키워드당 최대 10개 영상
  ├── DB 중복 체크 (videoId)
  ├── YouTube Videos API → 조회수, 좋아요, 영상 길이
  ├── 메타데이터 태깅 (region, country, city, theme)
  └── DB 저장 + @CacheEvict
```

### 인기 크리에이터

```
YouTubeChannelCollectJob
  │
  ├── 6개 키워드로 채널 검색
  ├── 필터링 (구독자 10만+, 방송사 제외, 중복 제거 → 최대 50개)
  ├── 채널별 최신 여행 영상 최대 3개 수집
  └── DB Upsert + @CacheEvict
```

### 장소 보강 재시도 (주기적)

```
PlaceEnrichRetryJob
  │
  ├── 대상: placeId IS NULL, category ≠ TRANSPORTATION, searchCount < 10
  └── Google Places API 재검색 → 성공 시 좌표 저장 / 실패 시 count++
```

---

## API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/login` | 디바이스 시리얼 기반 로그인 (신규 시 자동 가입) |
| POST | `/api/video/analyze` | 영상 분석 요청 (비동기, 202) |
| GET | `/api/video/{id}/schedule` | 분석 결과 일정표 + 장소 좌표 |
| GET | `/api/video/discover/category` | 인기 여행 영상 (국가/지역 필터) |
| GET | `/api/video/discover/theme` | 테마별 여행 영상 (커서 페이징, 40건) |
| GET | `/api/video/discover/channels` | 인기 크리에이터 + 최신 영상 (랜덤 10개) |
