package com.wanted.codebombalms.problems.hint.application.service;

import com.wanted.codebombalms.problems.hint.application.usecase.FindProblemHintsUseCase;
import com.wanted.codebombalms.problems.hint.application.port.LoadProblemHintPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemHintService implements FindProblemHintsUseCase {

    private final LoadProblemHintPort loadProblemHintPort;

    public ProblemHintService(LoadProblemHintPort loadProblemHintPort) {
        this.loadProblemHintPort = loadProblemHintPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemHintView> handle(Long problemId) {
        return loadProblemHintPort.findByProblemId(problemId)
                .stream()
                .map(hint -> new ProblemHintView(
                        hint.getHintId(),
                        hint.getHintOrder(),
                        hint.getHintContent()
                ))
                .toList();
    }
}
