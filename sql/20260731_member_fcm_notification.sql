-- 운영 DB 수동 마이그레이션 (prod: ddl-auto=validate 이므로 배포 전에 반드시 먼저 실행)
-- 대상: member 테이블 — FCM 토큰, 플랫폼, 알림 수신 여부 컬럼 추가
-- 실행법 (EC2에서):
--   docker exec -i linktrip-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" linktrip' < 20260731_member_fcm_notification.sql

ALTER TABLE member
    ADD COLUMN fcm_token VARCHAR(512) NULL,
    ADD COLUMN platform VARCHAR(20) NULL,
    ADD COLUMN notification_enabled BIT(1) NOT NULL DEFAULT b'1';
