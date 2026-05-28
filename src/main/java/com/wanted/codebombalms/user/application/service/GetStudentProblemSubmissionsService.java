package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import com.wanted.codebombalms.user.application.port.StudentProblemSubmissionQueryPort;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionResult;
import com.wanted.codebombalms.user.application.usecase.GetStudentProblemSubmissionsUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudentProblemSubmissionsService implements GetStudentProblemSubmissionsUseCase {

    private final UserRepository userRepository;
    private final StudentProblemSubmissionQueryPort queryPort;

    @Override
    public StudentProblemSubmissionResult getStudentProblemSubmissions(
            StudentProblemSubmissionQuery query
    ) {
        validateStudent(query.userId());

        return StudentProblemSubmissionResult.of(
                query.userId(),
                queryPort.findByCondition(query)
        );
    }

    private void validateStudent(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.STUDENT_NOT_FOUND));

        if (user.getRole() != UserRole.STUDENT || user.isDeleted()) {
            throw new NotFoundException(UserErrorCode.STUDENT_NOT_FOUND);
        }
    }
}
