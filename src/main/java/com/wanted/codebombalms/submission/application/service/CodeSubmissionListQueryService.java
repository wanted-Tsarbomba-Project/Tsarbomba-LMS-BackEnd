package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.SubmissionListQueryPort;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionListItem;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeSubmissionListQueryService implements CodeSubmissionListQueryUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final SubmissionListQueryPort submissionListQueryPort;

    public CodeSubmissionListQueryService(
            LoadProblemForSubmissionPort loadProblemForSubmissionPort,
            SubmissionListQueryPort submissionListQueryPort
    ) {
        this.loadProblemForSubmissionPort = loadProblemForSubmissionPort;
        this.submissionListQueryPort = submissionListQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeSubmissionPageView handle(Long problemId, int page, int size) {
        if (problemId == null) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }

        loadProblemForSubmissionPort.loadProblem(problemId);

        int safePage = page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size < 1 ? DEFAULT_SIZE : size;
        CodeSubmissionPage result = submissionListQueryPort.findCodeSubmissions(problemId, safePage, safeSize);

        return new CodeSubmissionPageView(
                result.submissions()
                        .stream()
                        .map(this::toItemView)
                        .toList(),
                new PageInfoView(
                        result.currentPage(),
                        result.totalPage(),
                        result.totalCount()
                )
        );
    }

    private CodeSubmissionListItemView toItemView(CodeSubmissionListItem item) {
        return new CodeSubmissionListItemView(
                item.submissionId(),
                item.problemId(),
                item.correct(),
                item.earnedScore(),
                item.passedTestCount(),
                item.totalTestCount(),
                item.executionStatus(),
                item.submittedAt()
        );
    }
}
