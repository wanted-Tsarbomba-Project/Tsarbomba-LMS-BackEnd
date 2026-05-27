package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.learning.domain.model.CourseLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LectureLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemStatistics;
import com.wanted.codebombalms.learning.domain.model.LearningProgressSummary;
import java.util.List;

public interface AdminLearningProgressQueryUseCase {

    List<CourseLearningProgress> findCourseProgresses();

    CourseLearningProgress findCourseProgress(Long courseId);

    List<StudentLearningProgress> findStudentProgresses(Long courseId);

    StudentLearningProgress findStudentProgress(Long courseId, Long userId);

    List<LectureLearningProgress> findLectureProgresses(Long courseId);

    LectureProblemStatistics findLectureProblemStatistics(Long lectureId);

    LearningProgressSummary summarizeLearningProgress();
}
