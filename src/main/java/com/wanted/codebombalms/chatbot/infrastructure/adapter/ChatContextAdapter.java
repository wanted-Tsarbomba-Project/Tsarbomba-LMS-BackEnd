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
        return problemRepository.findById(problemId)
                .flatMap(problem -> datasetRepository.findFirstByProblemSet_ProblemSetIdAndStatus(
                        problem.getProblemSet().getProblemSetId(),
                        "ACTIVE"
                ))
                .map(e -> new DatasetInfo(
                        e.getOriginalFileName(),
                        e.getFileUrl()
                ))
                .orElse(null);
    }

    @Override
    public CurrentProblemInfo findCurrentProblemInfo(Long userId, Long problemSetId) {
        int currentProblemNumber = progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .map(e -> e.getCurrentProblemNumber())
                .orElse(1);

        String problemSetTitle = problemSetRepository.findById(problemSetId)
                .map(e -> e.getTitle())
                .orElse(null);

        return problemRepository
                .findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(problemSetId, currentProblemNumber, "ACTIVE")
                .map(e -> new CurrentProblemInfo(e.getProblemId(), problemSetTitle, e.getTitle()))
                .orElse(new CurrentProblemInfo(null, problemSetTitle, null));
    }
}
