package com.wanted.codebombalms.domain.course.application.policy;

import com.wanted.codebombalms.course.application.policy.CourseAuthorPolicy;
import com.wanted.codebombalms.course.application.port.UserCatalogPort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseAuthorPolicy unit test")
class CourseAuthorPolicyTest {

    @Mock
    private UserCatalogPort userCatalogPort;

    @InjectMocks
    private CourseAuthorPolicy courseAuthorPolicy;

    @Test
    void validateOperator_passes_whenUserIsActiveOperator() {
        Long instructorId = 2L;
        given(userCatalogPort.isActiveOperator(instructorId)).willReturn(true);

        courseAuthorPolicy.validateOperator(instructorId);
    }

    @Test
    void validateOperator_throwsValidation_whenUserIsNotActiveOperator() {
        Long instructorId = 1L;
        given(userCatalogPort.isActiveOperator(instructorId)).willReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> courseAuthorPolicy.validateOperator(instructorId)
        );

        assertEquals(CourseErrorCode.COURSE_OPERATOR_REQUIRED, exception.getErrorCode());
    }
}
