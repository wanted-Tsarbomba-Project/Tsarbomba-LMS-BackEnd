-- ==============================================
-- problem_suggested_question : 문제별 추천 질문(자주 하는 질문 유형)
-- 적용 순서: 이 CREATE 를 각 환경 DB 에 먼저 적용한 뒤 새 코드를 배포한다.
--           (ddl-auto: validate 이므로 테이블이 없으면 부팅 실패)
-- 규칙:
--  - 문제 단위 공유(여러 유저 집계 결과) → user_id 없음.
--  - 재생성마다 통째 교체(delete + insert) → status/updated_at 없음.
--  - question = 배치가 생성한 대표 질문 1건, rank_no = 노출 순서(1..N).
-- ==============================================

CREATE TABLE problem_suggested_question (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    problem_set_id BIGINT       NOT NULL,
    problem_id     BIGINT       NOT NULL,
    question       VARCHAR(500) NOT NULL,
    rank_no        INT          NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_psq_problem (problem_set_id, problem_id)
);
