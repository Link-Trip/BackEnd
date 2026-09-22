-- 운영 DB 수동 마이그레이션 (prod: ddl-auto=validate 이므로 배포 전에 반드시 먼저 실행)
-- 대상: terms(약관), terms_agreement(회원별 동의 이력) 테이블 신규 생성 + 초기 약관 2건 시드
-- 실행법 (EC2에서):
--   docker exec -i linktrip-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" linktrip' < 20260922_terms.sql
--
-- ⚠️ detail_url은 현재 노션 내부 링크(비공개)로 시드되어 있음.
--    노션에서 "웹에 게시"한 공개 링크(*.notion.site)를 받으면 UPDATE로 교체할 것:
--    UPDATE terms SET detail_url = '<공개URL>' WHERE type = 'SERVICE' AND version = 1;

SET NAMES utf8mb4;

CREATE TABLE terms (
    id VARCHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    required BIT(1) NOT NULL,
    version INT NOT NULL,
    detail_url VARCHAR(512) NOT NULL,
    active BIT(1) NOT NULL,
    deleted BIT(1) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_terms_type_version (type, version)
);

CREATE TABLE terms_agreement (
    id VARCHAR(36) NOT NULL,
    member_id VARCHAR(36) NOT NULL,
    terms_id VARCHAR(36) NOT NULL,
    deleted BIT(1) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_terms_agreement_member (member_id)
);

INSERT INTO terms (id, type, title, required, version, detail_url, active, deleted, created_at, updated_at) VALUES
(UUID(), 'SERVICE', '서비스 이용약관 동의', b'1', 1, 'https://app.notion.com/p/a984a7b2f6aa830faff601ee3e4690e4', b'1', b'0', NOW(6), NOW(6)),
(UUID(), 'PRIVACY', '개인정보 수집·이용 동의', b'1', 1, 'https://app.notion.com/p/A-5244a7b2f6aa8278abb68135f7efedcf', b'1', b'0', NOW(6), NOW(6));
