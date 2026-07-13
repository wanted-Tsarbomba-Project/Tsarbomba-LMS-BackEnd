-- ==============================================
-- chat_message_feedback : AI 응답 품질 피드백(👍/👎)
-- 적용 순서: 이 CREATE 를 각 환경 DB 에 먼저 적용한 뒤 새 코드를 배포한다.
--           (ddl-auto: validate 이므로 테이블/컬럼이 없으면 부팅 실패)
-- 규칙:
--  - 채팅방은 1인 소유 → 메시지당 평가 1행 (uq_feedback_message).
--  - rating = 'UP' | 'DOWN' (FeedbackRating enum, STRING 매핑).
--  - user_id = 평가자(방 주인). message_id→room→user 로 유도 가능하나
--    소유검증·per-user 분석 편의를 위해 비정규화 저장한다.
--  - 취소는 행 삭제(hard delete).
-- ==============================================

CREATE TABLE chat_message_feedback (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    message_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    rating      VARCHAR(4)   NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_feedback_message UNIQUE (message_id),
    CONSTRAINT fk_feedback_message FOREIGN KEY (message_id)
        REFERENCES chat_message (message_id)
);
