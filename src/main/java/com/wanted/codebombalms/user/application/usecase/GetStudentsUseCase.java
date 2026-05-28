package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.application.query.StudentPageResult;

public interface GetStudentsUseCase {

    StudentPageResult getStudents(int page, int size);
}
