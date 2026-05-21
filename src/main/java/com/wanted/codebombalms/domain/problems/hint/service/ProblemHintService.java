package com.wanted.codebombalms.domain.problems.hint.service;

import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.hint.dto.response.ProblemHintResponse;
import com.wanted.codebombalms.domain.problems.hint.entity.ProblemHint;
import com.wanted.codebombalms.domain.problems.hint.repository.ProblemHintRepository;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemHintService {

    private final ProblemHintRepository problemHintRepository;

    public ProblemHintService(ProblemHintRepository problemHintRepository) {
        this.problemHintRepository = problemHintRepository;
    }

    @Transactional(readOnly = true)
    public List<ProblemHintResponse> findHints(Long problemId) {
        return problemHintRepository.findByProblem_ProblemIdOrderByHintOrderAsc(problemId)
                .stream()
                .map(ProblemHintResponse::new)
                .toList();
    }

    public ProblemHint createHint(Problem problem, String hintContent) {
        if (hintContent == null || hintContent.isBlank()) {
            return null;
        }

        ProblemHint problemHint = new ProblemHint(
                problem,
                1,
                hintContent
        );

        return problemHintRepository.save(problemHint);
    }

    public ProblemHint updateHint(Long hintId, String hintContent) {
        ProblemHint problemHint = problemHintRepository.findById(hintId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.HINT_NOT_FOUND));

        problemHint.update(hintContent);

        return problemHint;
    }

    public ProblemHint updateHint(Problem problem, Long hintId, String hintContent) {
        ProblemHint problemHint = problemHintRepository
                .findByHintIdAndProblem_ProblemId(hintId, problem.getProblemId())
                .orElseThrow(() -> new ValidationException(ProblemErrorCode.HINT_NOT_IN_PROBLEM));

        problemHint.update(hintContent);

        return problemHint;
    }

}
