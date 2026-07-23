package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface SpringDataSuggestedQuestionRepository extends JpaRepository<SuggestedQuestionJpaEntity, Long> {

    List<SuggestedQuestionJpaEntity> findByProblemSetIdAndProblemIdOrderByRankNoAsc(Long problemSetId, Long problemId);

    @Modifying
    void deleteByProblemSetIdAndProblemId(Long problemSetId, Long problemId);
}
