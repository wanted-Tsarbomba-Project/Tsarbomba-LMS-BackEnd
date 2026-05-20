package com.wanted.codebombalms.domain.problems.problem.service;

import com.wanted.codebombalms.domain.problems.problem.dto.response.ProblemResponse;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.repository.ProblemRepository;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemUpdateRequest;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public List<ProblemResponse> findProblemsByCategory(Long categoryId) {
        return problemRepository
                .findByProblemSet_Category_CategoryIdAndStatusOrderByProblemOrderAsc(categoryId, "ACTIVE")
                .stream()
                .map(ProblemResponse::new)
                .toList();
    }

    public ProblemResponse findCurrentProblem(Long problemSetId, Integer currentProblemNumber) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
                        problemSetId,
                        currentProblemNumber,
                        "ACTIVE"
                )
                .map(ProblemResponse::new)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.NO_CURRENT_PROBLEM));
    }

    public Problem findProblemEntity(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));
    }

    public Problem findProblemEntity(Long problemSetId, Long problemId) {
        return problemRepository.findByProblemIdAndProblemSet_ProblemSetId(problemId, problemSetId)
                .orElseThrow(() -> new RuntimeException("해당 문제 세트에 속한 소문제가 아닙니다."));
    }

    public Optional<Long> findProblemIdByProblemSetAndOrder(Long problemSetId, Integer problemOrder) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
                        problemSetId,
                        problemOrder,
                        "ACTIVE"
                )
                .map(Problem::getProblemId);
    }

    public List<Problem> findActiveProblemEntitiesByProblemSet(Long problemSetId) {
        return problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                "ACTIVE"
        );
    }

    public Optional<ProblemResponse> findLastProblem(Long problemSetId) {
        return problemRepository
                .findTopByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderDesc(problemSetId, "ACTIVE")
                .map(ProblemResponse::new);
    }

    public Problem createProblem(
            ProblemSet problemSet,
            ProblemCreateRequest request,
            Integer problemOrder
    ) {
        int score = request.point() == null ? 0 : request.point();

        Problem problem = new Problem(
                problemSet,
                request.title(),
                request.content(),
                request.answer(),
                request.explanation(),
                score,
                problemOrder
        );

        return problemRepository.save(problem);
    }

    public Problem createProblem(
            ProblemSet problemSet,
            ProblemUpdateRequest request,
            Integer problemOrder
    ) {
        int score = request.point() == null ? 0 : request.point();

        Problem problem = new Problem(
                problemSet,
                request.title(),
                request.content(),
                request.answer(),
                request.explanation(),
                score,
                problemOrder
        );

        return problemRepository.save(problem);
    }


    public Problem updateProblem(Long problemSetId, ProblemUpdateRequest request) {
        Problem problem = findProblemEntity(problemSetId, request.problemId());

        int score = request.point() == null ? 0 : request.point();

        problem.update(
                request.title(),
                request.content(),
                request.answer(),
                request.explanation(),
                score
        );

        return problem;
    }



}
