package com.wanted.codebombalms.domain.problems.set.service;

import com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory;
import com.wanted.codebombalms.domain.problems.category.service.ProblemCategoryService;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.hint.service.ProblemHintService;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import com.wanted.codebombalms.domain.problems.set.repository.ProblemSetRepository;
import com.wanted.codebombalms.global.error.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemSetRegistrationService {

    private final ProblemCategoryService problemCategoryService;
    private final ProblemSetRepository problemSetRepository;
    private final ProblemService problemService;
    private final ProblemHintService problemHintService;

    public ProblemSetRegistrationService(
            ProblemCategoryService problemCategoryService,
            ProblemSetRepository problemSetRepository,
            ProblemService problemService,
            ProblemHintService problemHintService
    ) {
        this.problemCategoryService = problemCategoryService;
        this.problemSetRepository = problemSetRepository;
        this.problemService = problemService;
        this.problemHintService = problemHintService;
    }

    @Transactional
    public ProblemSetCreateResponse createProblemSet(ProblemSetCreateRequest request) {
        validateCreateRequest(request);

        ProblemCategory category = problemCategoryService.findOrCreateActiveCategory(
                request.categoryName()
        );

        ProblemSet problemSet = new ProblemSet(
                category,
                request.title(),
                request.description(),
                "EASY",
                request.problems().size()
        );

        ProblemSet savedProblemSet = problemSetRepository.save(problemSet);

        int createdProblemCount = 0;

        for (int i = 0; i < request.problems().size(); i++) {
            ProblemCreateRequest problemRequest = request.problems().get(i);

            Problem savedProblem = problemService.createProblem(
                    savedProblemSet,
                    problemRequest,
                    i + 1
            );

            problemHintService.createHint(
                    savedProblem,
                    problemRequest.hint()
            );

            createdProblemCount++;
        }

        return new ProblemSetCreateResponse(savedProblemSet, createdProblemCount);
    }

    private void validateCreateRequest(ProblemSetCreateRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_TITLE_REQUIRED);
        }

        if (request.categoryName() == null || request.categoryName().isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        }

        if (request.problems() == null || request.problems().isEmpty()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_REQUIRED);
        }

        for (ProblemCreateRequest problem : request.problems()) {
            validateProblemRequest(problem);
        }
    }

    private void validateProblemRequest(ProblemCreateRequest problem) {
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
