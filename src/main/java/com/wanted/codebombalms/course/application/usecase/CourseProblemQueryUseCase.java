package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import java.util.List;

public interface CourseProblemQueryUseCase {

    List<CourseProblemSet> findProblemSetsByCourse(Long courseId);

    List<CourseProblemStep> findProblemsByLecture(Long lectureId);
}
