-- ============================================================
-- Load-test seed: bulk STUDENT rows
-- 목적: users(role, created_at) 인덱스 부재 → 풀스캔 + filesort 병목을
--       baseline 에서 드러내려면 학생 행이 충분히 많아야 한다(18명으론 안 보임).
-- 전제: 00_users.sql(이름있는 계정) 먼저 적재. user_id 는 AUTO_INCREMENT(1000~) 자동.
-- 재실행: loadtest 는 ddl-auto:update + 도커 볼륨이라 데이터가 영속된다. 따라서
--        INSERT IGNORE 로 중복(email/nickname UNIQUE)을 건너뛰어 재실행을 안전하게 한다.
-- ============================================================

SET SESSION cte_max_recursion_depth = 2000000;

INSERT IGNORE INTO users (
    role, email, password, name, nickname, phone,
    provider, provider_id, email_verified, is_locked,
    bio, career, created_at, updated_at, deleted_at
)
WITH RECURSIVE seq (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000000    -- 100만: 풀스캔+filesort 가 실제로 아파지는 규모
)
SELECT
    'STUDENT',
    CONCAT('load', n, '@test.com'),                                  -- email UNIQUE
    '$2a$10$oUxELBLbZtfCM8XqGQHqVub7HGaxVJC58IqhH..g97m8A1HNXhxJy',  -- = Test1234!
    CONCAT('부하학생', n),
    CONCAT('loaduser', n),                                           -- nickname UNIQUE
    CONCAT('010-9', LPAD(n, 7, '0')),
    'LOCAL',
    NULL,
    TRUE,
    FALSE,
    NULL,
    NULL,
    NOW() - INTERVAL n MINUTE,   -- created_at 분산 → ORDER BY created_at DESC 가 filesort 유발
    NOW(),
    NULL
FROM seq;
