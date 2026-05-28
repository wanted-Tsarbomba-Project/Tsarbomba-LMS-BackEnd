package com.wanted.codebombalms.user.application.port;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionItem;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;

import java.util.List;

public interface StudentProblemSubmissionQueryPort {

    List<StudentProblemSubmissionItem> findByCondition(StudentProblemSubmissionQuery query);
}
