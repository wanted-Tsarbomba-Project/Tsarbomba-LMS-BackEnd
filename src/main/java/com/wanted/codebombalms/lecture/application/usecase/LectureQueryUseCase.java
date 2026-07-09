package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.domain.model.Lecture;

import java.util.List;

public interface LectureQueryUseCase {

    List<Lecture> findLecturesByCourseId(Long courseId);

    List<Lecture> findLecturesByCourseIdForAccess(Long courseId, Long userId, boolean operator);

    Lecture findLectureById(Long lectureId);

    Lecture findLectureByIdForLearning(Long lectureId, Long userId, boolean operator);
}
