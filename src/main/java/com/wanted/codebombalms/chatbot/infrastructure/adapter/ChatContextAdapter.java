package com.wanted.codebombalms.chatbot.infrastructure.adapter;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.SpringDataProblemDatasetRepository;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.submission.infrastructure.persistence.SpringDataSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatContextAdapter implements ChatContextPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataProblemDatasetRepository datasetRepository;

    @Override
    public ProblemSetInfo findProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .map(e -> new ProblemSetInfo(
                        e.getProblemSetId(),
                        e.getTitle(),
                        e.getDescription()
                ))
                .orElse(null);
    }

    @Override
    public List<ProblemInfo> findProblems(Long problemSetId, Long userId) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(problemSetId, "ACTIVE")
                .stream()
                .map(p -> {
                    String submittedAnswer = submissionRepository
                            .findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(userId, p.getProblemId())
                            .map(s -> s.getSubmittedAnswer())
                            .orElse(null);
                    return new ProblemInfo(
                            p.getTitle(),
                            p.getContent(),
                            p.getProblemType(),
                            p.getAnswer(),
                            p.getExplanation(),
                            submittedAnswer
                    );
                })
                .toList();
    }

    @Override
    public SessionProgressInfo findSessionProgress(Long problemId) {
        int currentProblemNumber = problemRepository.findById(problemId)
                .map(e -> e.getProblemOrder())
                .orElse(1);
        return new SessionProgressInfo(currentProblemNumber);
    }

    @Override
    public DatasetInfo findDataset(Long problemSetId) {
        return datasetRepository
                .findFirstByProblemSet_ProblemSetIdAndStatusOrderByDatasetIdDesc(problemSetId, "ACTIVE")
                .map(e -> new DatasetInfo(e.getMetadata()))
                .orElse(null);
    }
}
