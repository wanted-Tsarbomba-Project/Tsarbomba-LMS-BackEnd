package com.wanted.codebombalms.lecture.domain.repository;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;
import java.util.Optional;

public interface LectureProblemSetRepository {

    LectureProblemSet save(LectureProblemSet lectureProblemSet);

    List<LectureProblemSet> findByCourseId(Long courseId);

    List<LectureProblemSet> findByCourseIdAndRole(Long courseId, LectureProblemSetRole role);

    List<LectureProblemSet> findByLectureId(Long lectureId);

    Optional<LectureProblemSet> findById(Long lectureProblemSetId);
}
