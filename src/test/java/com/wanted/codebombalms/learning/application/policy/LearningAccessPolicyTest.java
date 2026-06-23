package com.wanted.codebombalms.learning.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningAccessPolicyTest {

    @Mock
    private LearningEnrollmentPort learningEnrollmentPort;

    @InjectMocks
    private LearningAccessPolicy learningAccessPolicy;

    @Test
    void validateLectureProblemSetAccess_allowsStudent_whenStudentIsEnrolled() {
        Long userId = 10L;
        LearningLectureProblemSet lectureProblemSet = new LearningLectureProblemSet(6001L, 1L, 101L, 2001L);
        given(learningEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(true);

        assertDoesNotThrow(
                () -> learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet)
        );

        verify(learningEnrollmentPort).isActiveStudentOfCourse(1L, userId);
    }

    @Test
    void validateLectureProblemSetAccess_throwsForbidden_whenUserIdIsNull() {
        LearningLectureProblemSet lectureProblemSet = new LearningLectureProblemSet(6001L, 1L, 101L, 2001L);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> learningAccessPolicy.validateLectureProblemSetAccess(null, lectureProblemSet)
        );

        assertEquals(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED, exception.getErrorCode());
        verify(learningEnrollmentPort, never()).isActiveStudentOfCourse(1L, null);
    }

    @Test
    void validateLectureProblemSetAccess_throwsForbidden_whenStudentIsNotEnrolled() {
        Long userId = 10L;
        LearningLectureProblemSet lectureProblemSet = new LearningLectureProblemSet(6001L, 1L, 101L, 2001L);
        given(learningEnrollmentPort.isActiveStudentOfCourse(1L, userId)).willReturn(false);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> learningAccessPolicy.validateLectureProblemSetAccess(userId, lectureProblemSet)
        );

        assertEquals(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED, exception.getErrorCode());
    }
}
