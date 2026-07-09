package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;

public interface LectureProblemSetQueryUseCase {

    List<LectureProblemSet> findProblemSetsByCourse(Long courseId);

    List<LectureProblemSet> findProblemSetsByCourseForAccess(Long courseId, Long userId, boolean operator);

    List<LectureProblemSet> findProblemSetsByCourseAndRole(Long courseId, LectureProblemSetRole role);

    List<LectureProblemSet> findProblemSetsByLecture(Long lectureId);

    List<LectureProblemSet> findProblemSetsByLectureForAccess(Long lectureId, Long userId, boolean operator);

    LectureProblemSet findProblemSetById(Long lectureProblemSetId);
}
