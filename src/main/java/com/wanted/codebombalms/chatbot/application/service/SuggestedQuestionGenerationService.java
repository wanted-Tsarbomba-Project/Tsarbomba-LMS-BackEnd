package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.model.GeneratedProblemQuestions;
import com.wanted.codebombalms.chatbot.application.model.ProblemQuestionsGroup;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionGenerationClient;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionSourcePort;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionStoragePort;
import com.wanted.codebombalms.chatbot.application.usecase.GenerateSuggestedQuestionsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 추천 질문 생성 배치:
 *   임계값 넘는 문제의 USER 질문 수집 → FastAPI 생성 → problem_suggested_question 교체 저장.
 * 살아남은 대표 질문이 0건인 문제는 저장하지 않는다(서빙에서 DEFAULT 폴백).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestedQuestionGenerationService implements GenerateSuggestedQuestionsUseCase {

    // 튜닝 상수 (프로덕션 기준; 데모 데이터가 얇으면 낮춰 조정)
    static final int MIN_USERS = 3;
    static final int MIN_QUESTIONS = 10;
    static final int PER_PROBLEM_LIMIT = 150;
    static final int TOP_N = 5;

    private final SuggestedQuestionSourcePort sourcePort;
    private final SuggestedQuestionGenerationClient generationClient;
    private final SuggestedQuestionStoragePort storagePort;

    @Override
    public int generate() {
        List<ProblemQuestionsGroup> problems =
                sourcePort.findEligibleProblemQuestions(MIN_USERS, MIN_QUESTIONS, PER_PROBLEM_LIMIT);

        if (problems.isEmpty()) {
            log.info("event=suggested_question_generation_skipped reason=no_eligible_problem");
            return 0;
        }

        List<GeneratedProblemQuestions> generated = generationClient.generate(problems, TOP_N);

        int savedCount = 0;
        for (GeneratedProblemQuestions problem : generated) {
            if (problem.questions().isEmpty()) {
                continue;   // 대표 질문 0건 → 저장 안 함(서빙에서 DEFAULT)
            }
            storagePort.replace(problem.problemSetId(), problem.problemId(), problem.questions());
            savedCount++;
        }

        log.info("event=suggested_question_generation_completed eligible={} saved={}",
                problems.size(), savedCount);
        return savedCount;
    }
}
