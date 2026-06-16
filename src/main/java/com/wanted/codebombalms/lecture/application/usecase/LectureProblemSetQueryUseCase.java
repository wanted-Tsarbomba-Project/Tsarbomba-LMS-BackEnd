package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;

public interface LectureProblemSetQueryUseCase {

    List<LectureProblemSet> findProblemSetsByCourse(Long courseId);

    List<LectureProblemSet> findProblemSetsByCourseAndRole(Long courseId, LectureProblemSetRole role);

    List<LectureProblemSet> findProblemSetsByLecture(Long lectureId);

    LectureProblemSet findProblemSetById(Long lectureProblemSetId);
}
