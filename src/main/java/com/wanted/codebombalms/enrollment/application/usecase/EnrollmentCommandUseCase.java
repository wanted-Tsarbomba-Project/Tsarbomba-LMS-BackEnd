package com.wanted.codebombalms.enrollment.application.usecase;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;

public interface EnrollmentCommandUseCase {

    Enrollment createEnrollment(EnrollCourseCommand command);

    void cancelEnrollment(CancelEnrollmentCommand command);
}
