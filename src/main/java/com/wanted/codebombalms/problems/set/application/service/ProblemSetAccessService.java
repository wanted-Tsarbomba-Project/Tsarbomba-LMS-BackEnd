package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetAccessPort;
import com.wanted.codebombalms.problems.set.application.usecase.ValidateProblemSetAccessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSetAccessService implements ValidateProblemSetAccessUseCase {

    private final LoadProblemSetAccessPort loadProblemSetAccessPort;

    @Override
    public void validate(Long userId, Long problemSetId) {
        if (userId == null) {
            throw new ForbiddenException(ProblemErrorCode.ACCESS_DENIED);
        }

        var accessData = loadProblemSetAccessPort
                .loadAccessData(problemSetId)
                .orElseThrow(() -> new NotFoundException(
                        ProblemErrorCode.PROBLEM_SET_NOT_FOUND
                ));

        if (!accessData.isActive()) {
            throw new ForbiddenException(ProblemErrorCode.ACCESS_DENIED);
        }
    }
}
