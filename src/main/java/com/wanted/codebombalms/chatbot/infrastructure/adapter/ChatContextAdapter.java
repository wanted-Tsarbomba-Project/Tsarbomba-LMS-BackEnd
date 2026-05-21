package com.wanted.codebombalms.chatbot.infrastructure.adapter;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatContextAdapter implements ChatContextPort {

    @Override
    public ProblemSetInfo findProblemSet(Long problemSetId) {
        return null; // Phase 4에서 구현
    }

    @Override
    public ProblemInfo findProblem(Long problemId) {
        return null; // Phase 4에서 구현
    }

    @Override
    public SubmissionInfo findLatestSubmission(Long userId, Long problemId) {
        return null; // Phase 4에서 구현
    }

    @Override
    public SessionProgressInfo findSessionProgress(Long problemSetId) {
        return null; // Phase 4에서 구현
    }

    @Override
    public DatasetInfo findDataset(Long problemId) {
        return null; // Phase 4에서 구현
    }
}