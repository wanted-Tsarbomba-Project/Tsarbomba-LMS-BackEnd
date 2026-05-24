package com.wanted.codebombalms.course.application.policy;

import com.wanted.codebombalms.course.application.port.UserCatalogPort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAuthorPolicy {

    private final UserCatalogPort userCatalogPort;

    public void validateOperator(Long instructorId) {
        if (!userCatalogPort.isActiveOperator(instructorId)) {
            throw new ValidationException(CourseErrorCode.COURSE_OPERATOR_REQUIRED);
        }
    }
}
