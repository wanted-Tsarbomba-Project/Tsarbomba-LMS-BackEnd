package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.application.model.ProblemQuestionsGroup;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionSourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 추천 질문 생성 원천(USER 질문) 조회 포트의 JPA 구현. */
@Component
@RequiredArgsConstructor
public class SuggestedQuestionSourceAdapter implements SuggestedQuestionSourcePort {

    private final SuggestedQuestionSourceJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ProblemQuestionsGroup> findEligibleProblemQuestions(int minUsers, int minQuestions, int perProblemLimit) {
        return repository.findEligibleProblems(minUsers, minQuestions)
                .stream()
                .map(problem -> new ProblemQuestionsGroup(
                        problem.getProblemSetId(),
                        problem.getProblemId(),
                        repository.findRecentUserQuestions(
                                problem.getProblemSetId(), problem.getProblemId(), perProblemLimit)
                ))
                .toList();
    }
}
