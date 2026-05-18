package com.wanted.codebombalms.domain.problems.hint.service;

import com.wanted.codebombalms.domain.problems.hint.dto.response.ProblemHintResponse;
import com.wanted.codebombalms.domain.problems.hint.entity.ProblemHint;
import com.wanted.codebombalms.domain.problems.hint.repository.ProblemHintRepository;
import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
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
                .orElseThrow(() -> new RuntimeException("존재하지 않는 힌트입니다."));

        problemHint.update(hintContent);

        return problemHint;
    }

    public ProblemHint updateHint(Problem problem, Long hintId, String hintContent) {
        ProblemHint problemHint = problemHintRepository
                .findByHintIdAndProblem_ProblemId(hintId, problem.getProblemId())
                .orElseThrow(() -> new RuntimeException("해당 소문제에 속한 힌트가 아닙니다."));

        problemHint.update(hintContent);

        return problemHint;
    }

}
