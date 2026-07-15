package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
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
        validateCourseContentAccess(lecture.getCourse(), userId, operator);
        if (operator) {
            return;
        }
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }
        if (!lectureEnrollmentPort.isActiveStudentOfCourse(
                lecture.getCourse().getCourseId(),
                userId
        )) {
            throw new ForbiddenException(LectureErrorCode.LECTURE_ACCESS_DENIED);
        }
    }

    public void validateCourseContentAccess(Course course, Long userId, boolean operator) {
        if (course == null || course.getDeletedAt() != null || course.getStatus() == CourseStatus.DELETED) {
            throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        if (operator) {
            return;
        }
        if (course.getStatus() == CourseStatus.ACTIVE) {
            return;
        }
        if (course.getStatus() != CourseStatus.INACTIVE) {
            throw new ForbiddenException(LectureErrorCode.LECTURE_ACCESS_DENIED);
        }
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }
        if (!lectureEnrollmentPort.isActiveStudentOfCourse(
                course.getCourseId(),
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
