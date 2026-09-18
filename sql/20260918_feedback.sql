-- 운영 DB 수동 마이그레이션 (prod: ddl-auto=validate 이므로 배포 전에 반드시 먼저 실행)
-- 대상: feedback 테이블 신규 생성 (의견 전송 API)
-- member 테이블 변경 없음 — 탈퇴 마스킹은 기존 컬럼 값 변경만 사용.
-- 실행법 (EC2에서):
--   docker exec -i linktrip-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" linktrip' < 20260918_feedback.sql

CREATE TABLE feedback (
    id VARCHAR(36) NOT NULL,
    member_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    content VARCHAR(200) NOT NULL,
    app_version VARCHAR(20) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    os_version VARCHAR(20) NOT NULL,
    device_model VARCHAR(50) NOT NULL,
    deleted BIT(1) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_feedback_member_created (member_id, created_at)
);
