package com.wanted.codebombalms.problems.problem.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.domain.model.Problem;
import com.wanted.codebombalms.problems.problem.domain.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProblemQueryService {

    private final ProblemRepository problemRepository;

    public ProblemQueryService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProblemView> findProblemsByCategory(Long categoryId) {
        return problemRepository.findActiveProblemsByCategory(categoryId)
                .stream()
                .map(this::toView)
                .toList();
    }


    @Transactional(readOnly = true)
    public Problem findProblem(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ProblemForSubmissionView findProblemForSubmission(Long problemId) {
        Problem problem = findProblem(problemId);

        return new ProblemForSubmissionView(
                problem.getProblemId(),
                problem.getProblemSetId(),
                problem.getProblemOrder(),
                problem.getAnswer(),
                problem.getExplanation(),
                problem.getPoint(),
                problem.getAttemptLimit(),
                problem.getRetriable()
        );
    }


    @Transactional(readOnly = true)
    public Optional<Long> findProblemIdByProblemSetAndOrder(Long problemSetId, Integer problemOrder) {
        return problemRepository
                .findActiveProblemByProblemSetAndOrder(problemSetId, problemOrder)
                .map(Problem::getProblemId);
    }


    @Transactional(readOnly = true)
    public Optional<ProblemView> findLastProblem(Long problemSetId) {
        return problemRepository
                .findLastActiveProblem(problemSetId)
                .map(this::toView);
    }

    private ProblemView toView(Problem problem) {
        return new ProblemView(
                problem.getProblemId(),
                problem.getProblemOrder(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getPoint(),
                null
        );
    }

    public record ProblemView(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {
        public ProblemView withStartCode(String startCode) {
            return new ProblemView(
                    problemId,
                    problemNumber,
                    title,
                    content,
                    problemType,
                    point,
                    startCode
            );
        }
    }

    public record ProblemForSubmissionView(
            Long problemId,
            Long problemSetId,
            Integer problemOrder,
            String answer,
            String explanation,
            Integer point,
            Integer attemptLimit,
            Boolean retriable
    ) {
    }
}
