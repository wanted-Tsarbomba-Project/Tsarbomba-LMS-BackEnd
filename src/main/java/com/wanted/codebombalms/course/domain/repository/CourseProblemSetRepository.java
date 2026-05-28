package com.wanted.codebombalms.course.domain.repository;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.util.List;
import java.util.Optional;

public interface CourseProblemSetRepository {

    CourseProblemSet save(CourseProblemSet courseProblemSet);

    List<CourseProblemSet> findByCourseId(Long courseId);

    List<CourseProblemSet> findByCourseIdAndRole(Long courseId, CourseProblemSetRole role);

    List<CourseProblemSet> findByLectureId(Long lectureId);

    Optional<CourseProblemSet> findById(Long courseProblemSetId);
}
