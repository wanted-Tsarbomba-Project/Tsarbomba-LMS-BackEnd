-- ============================================================
-- Load-test seed: login_history 100만 행 (showcase A3)
-- 목적: login_history 인덱스 부재 → 풀스캔 + filesort 를 baseline 에서 드러냄.
--       u01(user_id=9)에 이력을 몰아 filesort 비용을 키운다.
-- 전제: 00_users.sql 먼저 적재. loadtest DB(도커 MySQL 3307) 전용.
-- 멱등: login_history_id 명시(1~100만) + INSERT IGNORE → 재실행 안전.
-- ============================================================

SET SESSION cte_max_recursion_depth = 2000000;

INSERT IGNORE INTO login_history (
    login_history_id, user_id, ip_address, user_agent,
    device_fp, country, city, is_suspicious, created_at
)
WITH RECURSIVE seq (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000000
)
SELECT
    n,
    9,
    CONCAT('10.', n % 256, '.', (n DIV 256) % 256, '.', n % 100),
    'Mozilla/5.0 (loadtest k6)',
    SHA2(CONCAT('dev', n), 256),
    'KR',
    'Seoul',
    FALSE,
    NOW() - INTERVAL n MINUTE
FROM seq;
