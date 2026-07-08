-- ==============================================
-- chat_message 토큰 사용량 컬럼 추가
-- 적용 순서: 이 ALTER 를 각 환경 DB 에 먼저 적용한 뒤 새 코드를 배포한다.
--           (ddl-auto: validate 이므로 컬럼이 없으면 부팅 실패)
-- prompt/completion/total = AI 응답 1건의 토큰 사용량. AI 행에만 값이 있고
-- USER 행·컬럼 추가 이전 과거 행은 NULL 이다(값 없음 ≠ 0).
-- ==============================================

ALTER TABLE chat_message
    ADD COLUMN prompt_tokens     INT NULL,
    ADD COLUMN completion_tokens INT NULL,
    ADD COLUMN total_tokens      INT NULL;
