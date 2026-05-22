package com.wanted.codebombalms.chatbot.infrastructure.adapter;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.SpringDataProblemDatasetRepository;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.progress.infrastructure.persistence.SpringDataProgressRepository;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.submission.infrastructure.persistence.SpringDataSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatContextAdapter implements ChatContextPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataProgressRepository progressRepository;
    private final SpringDataProblemDatasetRepository datasetRepository;

    @Override
    public ProblemSetInfo findProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .map(e -> new ProblemSetInfo(
                        e.getTitle(),
                        e.getDescription(),
                        e.getDifficulty(),
                        e.getCategory() != null ? e.getCategory().getCategoryName() : null
                ))
                .orElse(null);
    }

    @Override
    public ProblemInfo findProblem(Long problemId) {
        return problemRepository.findById(problemId)
                .map(e -> new ProblemInfo(
                        e.getProblemId(),
                        e.getProblemOrder(),
                        e.getTitle(),
                        e.getContent(),
                        e.getProblemType(),
                        e.getAnswer(),
                        e.getExplanation()
                ))
                .orElse(null);
    }

    @Override
    public SubmissionInfo findLatestSubmission(Long userId, Long problemId) {
        return submissionRepository
                .findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(userId, problemId)
                .map(e -> new SubmissionInfo(
                        e.getSubmittedAnswer(),
                        e.getCorrect() != null && e.getCorrect()
                ))
                .orElse(null);
    }

    @Override
    public SessionProgressInfo findSessionProgress(Long problemSetId) {
        return progressRepository.findByUserIdAndProblemSet_ProblemSetId(null, problemSetId)
                .map(e -> new SessionProgressInfo(
                        e.getCurrentProblemNumber(),
                        e.getProblemSet().getTotalProblemCount(),
                        List.of()
                ))
                .orElse(null);
    }

    @Override
    public DatasetInfo findDataset(Long problemId) {
        return datasetRepository.findFirstByProblem_ProblemIdAndStatus(problemId, "ACTIVE")
                .map(e -> new DatasetInfo(
                        e.getOriginalFileName(),
                        e.getFileUrl()
                ))
                .orElse(null);
    }
}