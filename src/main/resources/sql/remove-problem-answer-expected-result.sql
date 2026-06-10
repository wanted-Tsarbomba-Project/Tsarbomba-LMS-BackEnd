-- Java 배포 후 개발/운영 DB에서 수동 실행한다.
-- 실행 전 반드시 DB 백업과 대상 스키마를 확인한다.
ALTER TABLE problem DROP COLUMN answer;
ALTER TABLE problem_test_case DROP COLUMN expected_result;
