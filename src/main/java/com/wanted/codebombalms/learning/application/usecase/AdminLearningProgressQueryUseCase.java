package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import java.util.List;

public interface AdminLearningProgressQueryUseCase {

    List<StudentLearningProgress> findStudentProgresses(Long courseId);
}
