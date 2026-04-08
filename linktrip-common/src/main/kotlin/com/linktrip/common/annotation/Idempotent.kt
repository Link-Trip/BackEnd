package com.linktrip.common.annotation

/**
 * 멱등성 보장이 필요한 API 메서드에 선언한다.
 *
 * 클라이언트는 요청 헤더에 `Idempotency-Key`를 포함해야 하며,
 * 동일한 키로 중복 요청이 들어오면 다음과 같이 동작한다:
 * - 처리 중: 409 Conflict
 * - 처리 완료: 이전 응답 반환
 * - 처리 실패: 재시도 허용
 *
 * 헤더가 없으면 멱등성 검사 없이 정상 처리된다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent
