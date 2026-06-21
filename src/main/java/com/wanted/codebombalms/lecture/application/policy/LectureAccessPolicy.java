package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureAccessPolicy {

    private final LectureEnrollmentPort lectureEnrollmentPort;

    public void validateLearningContentAccess(Lecture lecture, Long userId, boolean operator) {
        if (operator) {
            return;
        }
        if (userId == null || !lectureEnrollmentPort.isActiveStudentOfCourse(
                lecture.getCourse().getCourseId(),
                userId
        )) {
            throw new ForbiddenException(LectureErrorCode.LECTURE_ACCESS_DENIED);
        }
    }
}
