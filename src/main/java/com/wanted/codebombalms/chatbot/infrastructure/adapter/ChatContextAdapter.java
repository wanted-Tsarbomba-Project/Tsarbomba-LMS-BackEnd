package com.wanted.codebombalms.chatbot.infrastructure.adapter;

import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.problems.dataset.application.service.ProblemDatasetQueryService;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import com.wanted.codebombalms.problems.set.application.service.ProblemSetQueryService;
import com.wanted.codebombalms.submission.application.service.SubmissionQueryService;
import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatContextAdapter implements ChatContextPort {

    // 교안 패턴: 타 BC SpringData 직접참조 제거 → 타 BC application service 경유
    private final ProblemSetQueryService problemSetQueryService;
    private final ProblemQueryService problemQueryService;
    private final SubmissionQueryService submissionQueryService;
    private final ProblemDatasetQueryService problemDatasetQueryService;

    @Override
    public ProblemSetInfo findProblemSet(Long problemSetId) {
        var ps = problemSetQueryService.findProblemSetDetail(problemSetId);
        return new ProblemSetInfo(ps.problemSetId(), ps.title(), ps.description());
    }

    @Override
    public List<ProblemInfo> findProblems(Long problemSetId, Long userId) {
        return problemQueryService.findProblemsForChat(problemSetId).stream()
                .map(p -> {
                    String submittedAnswer = submissionQueryService
                            .findLatestResult(userId, p.problemId())
                            .map(LatestSubmission::submittedAnswer)
                            .orElse(null);
                    return new ProblemInfo(
                            p.title(),
                            p.content(),
                            p.problemType(),
                            p.explanation(),
                            submittedAnswer
                    );
                })
                .toList();
    }

    @Override
    public SessionProgressInfo findSessionProgress(Long problemId) {
        int currentProblemNumber = problemQueryService
                .findProblemForSubmission(problemId)
                .problemOrder();
        return new SessionProgressInfo(currentProblemNumber);
    }

    @Override
    public String findProblemTitle(Long problemId) {
        return problemQueryService.findProblem(problemId).getTitle();
    }

    @Override
    public DatasetInfo findDataset(Long problemSetId) {
        return problemDatasetQueryService.findLatestActiveMetadata(problemSetId)
                .map(DatasetInfo::new)
                .orElse(null);
    }
}
