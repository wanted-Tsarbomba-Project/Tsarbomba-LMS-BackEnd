package com.wanted.codebombalms.domain.problems.set.service;

import com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory;
import com.wanted.codebombalms.domain.problems.category.service.ProblemCategoryService;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.hint.service.ProblemHintService;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemUpdateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import com.wanted.codebombalms.domain.problems.set.repository.ProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemSetUpdateService {

    private final ProblemSetRepository problemSetRepository;
    private final ProblemCategoryService problemCategoryService;
    private final ProblemService problemService;
    private final ProblemHintService problemHintService;

    public ProblemSetUpdateService(
            ProblemSetRepository problemSetRepository,
            ProblemCategoryService problemCategoryService,
            ProblemService problemService,
            ProblemHintService problemHintService
    ) {
        this.problemSetRepository = problemSetRepository;
        this.problemCategoryService = problemCategoryService;
        this.problemService = problemService;
        this.problemHintService = problemHintService;
    }

    @Transactional
    public ProblemSetUpdateResponse updateProblemSet(Long problemSetId, ProblemSetUpdateRequest request) {
        validateUpdateRequest(request);

        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        ProblemCategory category = problemCategoryService.findOrCreateActiveCategory(
                request.categoryName()
        );

        problemSet.update(
                category,
                request.title(),
                request.description()
        );

        int updatedProblemCount = 0;

        for (int i = 0; i < request.problems().size(); i++) {
            ProblemUpdateRequest problemRequest = request.problems().get(i);
            Problem problem = updateOrCreateProblem(problemSet, problemRequest, i + 1);
            updateOrCreateHint(problem, problemRequest);
            updatedProblemCount++;
        }

        problemSet.updateTotalProblemCount(request.problems().size());

        return new ProblemSetUpdateResponse(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getCategory().getCategoryName(),
                updatedProblemCount
        );
    }

    private Problem updateOrCreateProblem(
            ProblemSet problemSet,
            ProblemUpdateRequest problemRequest,
            Integer problemOrder
    ) {
        if (problemRequest.problemId() == null) {
            return problemService.createProblem(problemSet, problemRequest, problemOrder);
        }

        return problemService.updateProblem(problemSet.getProblemSetId(), problemRequest);
    }

    private void updateOrCreateHint(Problem problem, ProblemUpdateRequest problemRequest) {
        if (problemRequest.hint() == null || problemRequest.hint().isBlank()) {
            return;
        }

        if (problemRequest.hintId() == null) {
            problemHintService.createHint(problem, problemRequest.hint());
            return;
        }

        problemHintService.updateHint(problem, problemRequest.hintId(), problemRequest.hint());
    }

    private void validateUpdateRequest(ProblemSetUpdateRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_TITLE_REQUIRED);
        }

        if (request.categoryName() == null || request.categoryName().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        }

        if (request.problems() == null || request.problems().isEmpty()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_REQUIRED);
        }

        for (ProblemUpdateRequest problem : request.problems()) {
            validateProblemRequest(problem);
        }
    }

    private void validateProblemRequest(ProblemUpdateRequest problem) {
        if (problem.title() == null || problem.title().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        }

        if (problem.content() == null || problem.content().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        }

        if (problem.answer() == null || problem.answer().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_ANSWER_REQUIRED);
        }
    }
}
