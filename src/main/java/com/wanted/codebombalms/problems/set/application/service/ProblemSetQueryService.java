package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.CheckProblemSetCategoryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetPort;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummaryPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemSetQueryService implements GetProblemSetsUseCase {

    private final CheckProblemSetCategoryPort checkProblemSetCategoryPort;
    private final LoadProblemSetPort loadProblemSetPort;

    @Override
    @Transactional(readOnly = true)
    public ProblemSetPageView handle(GetProblemSetsQuery query) {
        validatePageRequest(query.page(), query.size());

        ProblemSetSummaryPage problemSets;

        if (query.categoryId() == null) {
            problemSets = loadProblemSetPort.loadActiveProblemSets(query.page(), query.size());
            return toPageView(problemSets);
        }

        if (!checkProblemSetCategoryPort.existsActiveCategory(query.categoryId())) {
            throw new NotFoundException(ProblemErrorCode.CATEGORY_NOT_FOUND);
        }

        problemSets = loadProblemSetPort.loadActiveProblemSetsByCategory(
                query.categoryId(),
                query.page(),
                query.size()
        );
        return toPageView(problemSets);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }
    }

    private ProblemSetPageView toPageView(ProblemSetSummaryPage page) {
        return new ProblemSetPageView(
                page.content()
                .stream()
                .map(this::toView)
                .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext()
        );
    }

    // === 챗봇 adapter용 메서드 ===
    // chatbot ChatContextAdapter.findProblemSet() 가 호출
    @Transactional(readOnly = true)
    public ProblemSetDetailView findProblemSetDetail(Long problemSetId) {
        return loadProblemSetPort.loadById(problemSetId)
                .map(ps -> new ProblemSetDetailView(
                        ps.getProblemSetId(),
                        ps.getTitle(),
                        ps.getDescription()))
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));
    }

    // 챗봇 adapter 응답 DTO
    public record ProblemSetDetailView(
            Long problemSetId,
            String title,
            String description
    ) {}

    private ProblemSetSummaryView toView(ProblemSetSummary problemSet) {
        return new ProblemSetSummaryView(
                problemSet.getProblemSetId(),
                problemSet.getProblemNumber(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getAccuracyRate(),
                problemSet.getCreatedAt()
        );
    }
}
