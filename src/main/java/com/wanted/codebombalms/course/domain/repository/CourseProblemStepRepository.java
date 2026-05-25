package com.wanted.codebombalms.course.domain.repository;

import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import java.util.List;

public interface CourseProblemStepRepository {

    CourseProblemStep save(CourseProblemStep courseProblemStep);

    List<CourseProblemStep> findByLectureId(Long lectureId);

    List<CourseProblemStep> findByCourseProblemSetId(Long courseProblemSetId);

    void deleteByCourseProblemSetId(Long courseProblemSetId);
}
