package com.wanted.codebombalms.chatbot.infrastructure.persistence;

import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** 추천 질문 저장·조회 포트의 JPA 구현. 재생성은 delete + insert 통째 교체. */
@Component
@RequiredArgsConstructor
public class SuggestedQuestionStorageAdapter implements SuggestedQuestionStoragePort {

    private final SpringDataSuggestedQuestionRepository repository;

    @Override
    @Transactional
    public void replace(Long problemSetId, Long problemId, List<String> questions) {
        repository.deleteByProblemSetIdAndProblemId(problemSetId, problemId);

        List<SuggestedQuestionJpaEntity> entities = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            entities.add(SuggestedQuestionJpaEntity.of(problemSetId, problemId, questions.get(index), index + 1));
        }
        repository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findQuestions(Long problemSetId, Long problemId) {
        return repository.findByProblemSetIdAndProblemIdOrderByRankNoAsc(problemSetId, problemId)
                .stream()
                .map(SuggestedQuestionJpaEntity::getQuestion)
                .toList();
    }
}
