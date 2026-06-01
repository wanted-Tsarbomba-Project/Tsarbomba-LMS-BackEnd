package com.wanted.codebombalms.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSubmissionTestResultRepository extends JpaRepository<SubmissionTestResultJpaEntity, Long> {

    List<SubmissionTestResultJpaEntity> findBySubmission_SubmissionIdOrderByTestCase_TestOrderAsc(Long submissionId);
}
