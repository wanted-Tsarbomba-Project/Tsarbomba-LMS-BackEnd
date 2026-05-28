package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionResult;

public interface GetStudentProblemSubmissionsUseCase {

    StudentProblemSubmissionResult getStudentProblemSubmissions(
            StudentProblemSubmissionQuery query
    );
}
