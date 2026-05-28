package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningUserPort;
import com.wanted.codebombalms.learning.domain.model.CourseLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import com.wanted.codebombalms.learning.domain.model.LearningProgressSummary;
import com.wanted.codebombalms.learning.domain.model.LectureLearningProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemStatistics;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLearningProgressQueryService implements AdminLearningProgressQueryUseCase {

    private final LearningCoursePort learningCoursePort;
    private final LearningEnrollmentPort learningEnrollmentPort;
    private final LearningLecturePort learningLecturePort;
    private final LearningCourseProblemPort learningCourseProblemPort;
    private final LearningUserPort learningUserPort;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseLearningProgress> findCourseProgresses() {
        return learningCoursePort.findActiveCourses()
                .stream()
                .map(this::buildCourseProgress)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseLearningProgress findCourseProgress(Long courseId) {
        return buildCourseProgress(learningCoursePort.findActiveCourse(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentLearningProgress> findStudentProgresses(Long courseId) {
        List<Long> studentIds = learningEnrollmentPort.findActiveStudentIdsByCourse(courseId);
        return studentIds.stream()
                .map(studentId -> buildStudentProgress(courseId, studentId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentLearningProgress findStudentProgress(Long courseId, Long userId) {
        return buildStudentProgress(courseId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureLearningProgress> findLectureProgresses(Long courseId) {
        long totalStudentCount = learningEnrollmentPort.findActiveStudentIdsByCourse(courseId).size();

        return learningLecturePort.findLecturesByCourse(courseId)
                .stream()
                .map(lecture -> LectureLearningProgress.of(
                        lecture.lectureId(),
                        lecture.title(),
                        lectureProgressRepository.countCompletedByLectureId(lecture.lectureId()),
                        totalStudentCount
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProblemStatistics findLectureProblemStatistics(Long lectureId) {
        List<Long> lectureProblemSetIds = learningCourseProblemPort.findLectureProblemSetIdsByLecture(lectureId);
        Long courseId = learningLecturePort.findCourseIdByLecture(lectureId);
        long totalStudentCount = learningEnrollmentPort.findActiveStudentIdsByCourse(courseId).size();
        long totalProblemSetCount = lectureProblemSetIds.size() * totalStudentCount;

        return LectureProblemStatistics.of(
                lectureId,
                lectureProblemProgressRepository.countCompletedByLectureProblemSetIds(lectureProblemSetIds),
                totalProblemSetCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LearningProgressSummary summarizeLearningProgress() {
        List<CourseLearningProgress> courseProgresses = findCourseProgresses();

        return LearningProgressSummary.of(
                courseProgresses.size(),
                courseProgresses.stream().mapToLong(CourseLearningProgress::enrolledStudentCount).sum(),
                courseProgresses.stream().mapToLong(CourseLearningProgress::completedLectureCount).sum(),
                courseProgresses.stream().mapToLong(CourseLearningProgress::totalLectureCount).sum(),
                courseProgresses.stream().mapToLong(CourseLearningProgress::completedProblemCount).sum(),
                courseProgresses.stream().mapToLong(CourseLearningProgress::totalProblemCount).sum()
        );
    }

    private CourseLearningProgress buildCourseProgress(LearningCourse course) {
        List<StudentLearningProgress> studentProgresses = findStudentProgresses(course.courseId());

        return CourseLearningProgress.of(
                course.courseId(),
                course.title(),
                studentProgresses.size(),
                studentProgresses.stream().mapToLong(StudentLearningProgress::completedLectureCount).sum(),
                studentProgresses.stream().mapToLong(StudentLearningProgress::totalLectureCount).sum(),
                studentProgresses.stream().mapToLong(StudentLearningProgress::completedProblemCount).sum(),
                studentProgresses.stream().mapToLong(StudentLearningProgress::totalProblemCount).sum()
        );
    }

    private StudentLearningProgress buildStudentProgress(Long courseId, Long studentId) {
        List<Long> lectureIds = learningLecturePort.findLectureIdsByCourse(courseId);
        List<Long> lectureProblemSetIds = learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId);

        return StudentLearningProgress.of(
                studentId,
                learningUserPort.findUserName(studentId),
                lectureProgressRepository.countCompletedByUserIdAndLectureIds(studentId, lectureIds),
                lectureIds.size(),
                lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                        studentId,
                        lectureProblemSetIds
                ),
                lectureProblemSetIds.size()
        );
    }
}
