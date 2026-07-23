package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.model.SuggestedQuestionsResult;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionStoragePort;
import com.wanted.codebombalms.chatbot.application.usecase.GetSuggestedQuestionsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 저장된 추천 질문을 조회하고, 없으면 DEFAULT 기본칩을 돌려준다. */
@Service
@RequiredArgsConstructor
public class SuggestedQuestionQueryService implements GetSuggestedQuestionsUseCase {

    // 생성 결과가 없을 때 보여줄 정적 기본칩 (정답구걸 제외 원칙 정렬: 힌트/디버깅/개념)
    static final List<String> DEFAULT_QUESTIONS = List.of(
            "이 문제 풀이 힌트를 주세요",
            "제가 제출한 코드가 왜 틀렸는지 봐주세요",
            "이 문제에 필요한 개념을 설명해주세요"
    );

    private final SuggestedQuestionStoragePort storagePort;

    @Override
    public SuggestedQuestionsResult get(Long problemSetId, Long problemId) {
        List<String> questions = storagePort.findQuestions(problemSetId, problemId);
        if (questions.isEmpty()) {
            return SuggestedQuestionsResult.defaults(DEFAULT_QUESTIONS);
        }
        return SuggestedQuestionsResult.generated(questions);
    }
}
