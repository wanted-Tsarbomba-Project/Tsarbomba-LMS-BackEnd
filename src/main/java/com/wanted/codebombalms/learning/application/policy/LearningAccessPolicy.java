package com.wanted.codebombalms.learning.application.policy;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LearningAccessPolicy {

    private final LearningEnrollmentPort learningEnrollmentPort;

    public void validateLectureProblemSetAccess(Long userId, LearningLectureProblemSet lectureProblemSet) {
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }
        if (!learningEnrollmentPort.isActiveStudentOfCourse(
                lectureProblemSet.courseId(),
                userId
        )) {
            throw new ForbiddenException(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED);
        }
    }

    public void validateStudentLectureProblemSetAccess(
            Long courseId,
            Long userId,
            LearningLectureProblemSet lectureProblemSet
    ) {
        if (courseId == null || !courseId.equals(lectureProblemSet.courseId())) {
            throw new ForbiddenException(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED);
        }

        validateLectureProblemSetAccess(userId, lectureProblemSet);
    }
}
