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
import com.wanted.codebombalms.learning.domain.model.StudentLearningProgressPage;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wanted.codebombalms.learning.infrastructure.metrics.LearningMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLearningProgressQueryService implements AdminLearningProgressQueryUseCase {

    private static final int STUDENT_PROGRESS_PAGE_SIZE = 20;

    private final LearningCoursePort learningCoursePort;
    private final LearningEnrollmentPort learningEnrollmentPort;
    private final LearningLecturePort learningLecturePort;
    private final LearningCourseProblemPort learningCourseProblemPort;
    private final LearningUserPort learningUserPort;
    private final LectureProgressRepository lectureProgressRepository;
    private final LectureProblemProgressRepository lectureProblemProgressRepository;
    private final LearningMetrics learningMetrics;

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

        return buildStudentProgresses(courseId, studentIds, studentIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentLearningProgressPage findStudentProgresses(Long courseId, int page) {
        int requestedPage = Math.max(page, 0);
        long totalStudentCount = learningEnrollmentPort.countActiveStudentsByCourse(courseId);
        List<Long> studentIds = learningEnrollmentPort.findActiveStudentIdsByCourse(
                courseId,
                requestedPage,
                STUDENT_PROGRESS_PAGE_SIZE
        );

        return StudentLearningProgressPage.of(
                buildStudentProgresses(courseId, studentIds, totalStudentCount),
                requestedPage,
                STUDENT_PROGRESS_PAGE_SIZE,
                totalStudentCount
        );
    }

    private List<StudentLearningProgress> buildStudentProgresses(
            Long courseId,
            List<Long> studentIds,
            long totalStudentCount
    ) {
        long totalStartedAt = System.nanoTime();

        log.info("event=learning_student_ids_queried courseId={} studentCount={} totalStudentCount={}",
                courseId, studentIds.size(), totalStudentCount);

        StudentProgressTiming timing = new StudentProgressTiming();
        long progressBuildStartedAt = System.nanoTime();
        List<StudentLearningProgress> progresses = new ArrayList<>();

        List<Long> lectureIds = List.of();
        List<Long> lectureProblemSetIds = List.of();
        Map<Long, String> userNames = Map.of();
        Map<Long, Long> completedLectureCounts = Map.of();
        Map<Long, Long> completedProblemCounts = Map.of();
        if (!studentIds.isEmpty()) {
            long lectureIdsStartedAt = System.nanoTime();
            lectureIds = learningLecturePort.findLectureIdsByCourse(courseId);
            timing.recordLectureIds(System.nanoTime() - lectureIdsStartedAt, lectureIds.size());

            long problemSetIdsStartedAt = System.nanoTime();
            lectureProblemSetIds = learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId);
            timing.recordProblemSetIds(System.nanoTime() - problemSetIdsStartedAt, lectureProblemSetIds.size());

            long userNameStartedAt = System.nanoTime();
            userNames = learningUserPort.findUserNames(studentIds);
            timing.recordUserName(System.nanoTime() - userNameStartedAt);

            long completedLectureCountStartedAt = System.nanoTime();
            completedLectureCounts = lectureProgressRepository.countCompletedByUserIdsAndLectureIds(
                    studentIds,
                    lectureIds
            );
            timing.recordCompletedLectureCount(System.nanoTime() - completedLectureCountStartedAt);

            long completedProblemCountStartedAt = System.nanoTime();
            completedProblemCounts = lectureProblemProgressRepository.countCompletedByUserIdsAndLectureProblemSetIds(
                    studentIds,
                    lectureProblemSetIds
            );
            timing.recordCompletedProblemCount(System.nanoTime() - completedProblemCountStartedAt);
        }

        for (Long studentId : studentIds) {
            progresses.add(buildStudentProgress(
                    studentId,
                    userNames,
                    completedLectureCounts,
                    lectureIds.size(),
                    completedProblemCounts,
                    lectureProblemSetIds.size()
            ));
        }
        long progressBuildElapsedNanos = System.nanoTime() - progressBuildStartedAt;

        log.info("event=learning_student_progress_built courseId={} studentCount={} totalStudentCount={} durationMs={}",
                courseId, studentIds.size(), totalStudentCount, progressBuildElapsedNanos / 1_000_000);
        log.info(
                "event=learning_student_progress_breakdown courseId={} studentCount={} totalStudentCount={} lectureCount={} "
                        + "problemSetCount={} lectureIdsMs={} problemSetIdsMs={} userNameMs={} "
                        + "completedLectureCountMs={} completedProblemCountMs={} totalBuildMs={}",
                courseId,
                studentIds.size(),
                totalStudentCount,
                timing.lectureCount,
                timing.problemSetCount,
                timing.lectureIdsElapsedNanos / 1_000_000,
                timing.problemSetIdsElapsedNanos / 1_000_000,
                timing.userNameElapsedNanos / 1_000_000,
                timing.completedLectureCountElapsedNanos / 1_000_000,
                timing.completedProblemCountElapsedNanos / 1_000_000,
                progressBuildElapsedNanos / 1_000_000
        );

        long totalElapsedNanos = System.nanoTime() - totalStartedAt;
        learningMetrics.recordStudentProgressQuery(totalElapsedNanos);
        log.info("event=learning_student_progress_queried courseId={} studentCount={} totalStudentCount={} durationMs={}",
                courseId, studentIds.size(), totalStudentCount, totalElapsedNanos / 1_000_000);

        return progresses;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentLearningProgress findStudentProgress(Long courseId, Long userId) {
        List<Long> lectureIds = learningLecturePort.findLectureIdsByCourse(courseId);
        List<Long> lectureProblemSetIds = learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId);

        return buildStudentProgress(userId, lectureIds, lectureProblemSetIds, null);
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

    private StudentLearningProgress buildStudentProgress(
            Long studentId,
            Map<Long, String> userNames,
            Map<Long, Long> completedLectureCounts,
            int totalLectureCount,
            Map<Long, Long> completedProblemCounts,
            int totalProblemCount
    ) {
        long itemStartedAt = System.nanoTime();

        StudentLearningProgress progress = StudentLearningProgress.of(
                studentId,
                userNames.getOrDefault(studentId, "사용자없음"),
                completedLectureCounts.getOrDefault(studentId, 0L),
                totalLectureCount,
                completedProblemCounts.getOrDefault(studentId, 0L),
                totalProblemCount
        );

        learningMetrics.recordStudentProgressItem(System.nanoTime() - itemStartedAt);
        return progress;
    }

    private StudentLearningProgress buildStudentProgress(
            Long studentId,
            List<Long> lectureIds,
            List<Long> lectureProblemSetIds,
            StudentProgressTiming timing
    ) {
        long itemStartedAt = System.nanoTime();

        long userNameStartedAt = System.nanoTime();
        String userName = learningUserPort.findUserName(studentId);
        if (timing != null) {
            timing.recordUserName(System.nanoTime() - userNameStartedAt);
        }

        long completedLectureCountStartedAt = System.nanoTime();
        long completedLectureCount = lectureProgressRepository.countCompletedByUserIdAndLectureIds(
                studentId,
                lectureIds
        );
        if (timing != null) {
            timing.recordCompletedLectureCount(System.nanoTime() - completedLectureCountStartedAt);
        }

        long completedProblemCountStartedAt = System.nanoTime();
        long completedProblemCount = lectureProblemProgressRepository.countCompletedByUserIdAndLectureProblemSetIds(
                studentId,
                lectureProblemSetIds
        );
        if (timing != null) {
            timing.recordCompletedProblemCount(System.nanoTime() - completedProblemCountStartedAt);
        }

        long itemElapsedNanos = System.nanoTime() - itemStartedAt;
        learningMetrics.recordStudentProgressItem(itemElapsedNanos);

        return StudentLearningProgress.of(
                studentId,
                userName,
                completedLectureCount,
                lectureIds.size(),
                completedProblemCount,
                lectureProblemSetIds.size()
        );
    }

    private static class StudentProgressTiming {

        private long lectureIdsElapsedNanos;
        private long problemSetIdsElapsedNanos;
        private long userNameElapsedNanos;
        private long completedLectureCountElapsedNanos;
        private long completedProblemCountElapsedNanos;
        private int lectureCount;
        private int problemSetCount;

        private void recordLectureIds(long elapsedNanos, int count) {
            lectureIdsElapsedNanos += elapsedNanos;
            lectureCount = count;
        }

        private void recordProblemSetIds(long elapsedNanos, int count) {
            problemSetIdsElapsedNanos += elapsedNanos;
            problemSetCount = count;
        }

        private void recordUserName(long elapsedNanos) {
            userNameElapsedNanos += elapsedNanos;
        }

        private void recordCompletedLectureCount(long elapsedNanos) {
            completedLectureCountElapsedNanos += elapsedNanos;
        }

        private void recordCompletedProblemCount(long elapsedNanos) {
            completedProblemCountElapsedNanos += elapsedNanos;
        }
    }
}
