CREATE TABLE IF NOT EXISTS lecture_problem_submission (
    lecture_problem_submission_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    lecture_problem_set_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    submitted_code TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    attempt_no INT NOT NULL,
    passed_test_count INT NOT NULL,
    total_test_count INT NOT NULL,
    execution_status VARCHAR(255) NULL,
    error_message TEXT NULL,
    submitted_at DATETIME(6) NOT NULL,
    PRIMARY KEY (lecture_problem_submission_id),
    INDEX idx_lecture_problem_submission_progress (
        user_id,
        lecture_problem_set_id,
        submitted_at
    ),
    INDEX idx_lecture_problem_submission_attempt (
        user_id,
        lecture_problem_set_id,
        problem_id
    )
);
