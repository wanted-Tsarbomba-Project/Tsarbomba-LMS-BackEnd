package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import com.wanted.codebombalms.lecture.application.port.LectureProgressPort;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureAccessPolicy {

    private final LectureEnrollmentPort lectureEnrollmentPort;
    private final LectureProgressPort lectureProgressPort;

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

    public void validatePreviousLecturesCompleted(Long userId, List<Long> previousLectureIds) {
        if (previousLectureIds.isEmpty()) {
            return;
        }
        if (!lectureProgressPort.areLecturesCompleted(userId, previousLectureIds)) {
            throw new ForbiddenException(LectureErrorCode.PREVIOUS_LECTURE_NOT_COMPLETED);
        }
    }
}
