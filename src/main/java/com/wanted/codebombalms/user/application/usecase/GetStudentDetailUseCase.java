package com.wanted.codebombalms.user.application.usecase;

import com.wanted.codebombalms.user.application.query.StudentDetail;

public interface GetStudentDetailUseCase {

    StudentDetail getStudentDetail(Long userId);
}
