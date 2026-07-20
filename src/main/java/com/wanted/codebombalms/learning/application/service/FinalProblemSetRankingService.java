package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemExplanationPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningContext;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningProfile;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationRequest;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationResult;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LectureContext;
import com.wanted.codebombalms.learning.application.usecase.FinalProblemSetRankingUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** MAIN 학습 기록과 강좌 문맥을 조립해 Python 추천 순위를 요청합니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinalProblemSetRankingService implements FinalProblemSetRankingUseCase {

    private static final int RECOMMENDATION_COUNT = 2;
    private static final int MAX_EXCLUDED_PROBLEM_SET_COUNT = 100;
    private static final int MAX_LECTURE_CONTEXT_COUNT = 100;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final LearningRecommendationClient learningRecommendationClient;
    private final LearningCoursePort learningCoursePort;
    private final LearningLecturePort learningLecturePort;
    private final LearningCourseProblemPort learningCourseProblemPort;
    private final LearningLectureProblemSetPort learningLectureProblemSetPort;
    private final LearningProblemPort learningProblemPort;
    private final LearningProblemExplanationPort learningProblemExplanationPort;
    private final LectureProblemSubmissionRepository lectureProblemSubmissionRepository;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LearningRecommendationResult rankFinalProblemSets(
            Long userId,
            Long courseId,
            Long lectureId,
            Long problemCategoryId,
            Set<Long> excludedProblemSetIds,
            boolean operator
    ) {
        if (excludedProblemSetIds.size() > MAX_EXCLUDED_PROBLEM_SET_COUNT) {
            log.warn(
                    "event=learning_recommendation_skipped reason=excluded_problem_set_limit count={}",
                    excludedProblemSetIds.size()
            );
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_UNAVAILABLE);
        }

        LearningProfile profile = operator || userId == null
                ? LearningProfile.empty()
                : buildLearningProfile(userId, courseId);
        LearningContext context = buildLearningContext(courseId);

        LearningRecommendationRequest request = new LearningRecommendationRequest(
                courseId,
                lectureId,
                problemCategoryId,
                excludedProblemSetIds.stream().sorted().toList(),
                RECOMMENDATION_COUNT,
                profile,
                context
        );
        return learningRecommendationClient.rankFinalProblemSets(request);
    }

    private LearningProfile buildLearningProfile(Long userId, Long courseId) {
        List<Long> mainLectureProblemSetIds =
                learningCourseProblemPort.findMainLectureProblemSetIdsByCourse(courseId);
        if (mainLectureProblemSetIds.isEmpty()) {
            return LearningProfile.empty();
        }

        Map<Long, LearningProblemPort.ProblemDetailForLearning> problemsById = new LinkedHashMap<>();
        Map<Long, List<LectureProblemSubmission>> submissionsByProblemId = new HashMap<>();

        for (Long lectureProblemSetId : mainLectureProblemSetIds) {
            Long problemSetId = learningLectureProblemSetPort
                    .findLectureProblemSet(lectureProblemSetId)
                    .problemSetId();
            LearningProblemPort.ProblemSetForLearning problemSet =
                    learningProblemPort.loadProblemSet(problemSetId);
            for (LearningProblemPort.ProblemDetailForLearning problem : problemSet.problems()) {
                problemsById.putIfAbsent(problem.problemId(), problem);
            }

            for (LectureProblemSubmission submission :
                    lectureProblemSubmissionRepository.findByUserIdAndLectureProblemSetId(
                            userId,
                            lectureProblemSetId
                    )) {
                if (problemsById.containsKey(submission.problemId())) {
                    submissionsByProblemId
                            .computeIfAbsent(submission.problemId(), ignored -> new ArrayList<>())
                            .add(submission);
                }
            }
        }

        int totalProblemCount = problemsById.size();
        if (totalProblemCount == 0) {
            return LearningProfile.empty();
        }

        Set<Long> viewedProblemIds = learningProblemExplanationPort.findViewedProblemIds(
                userId,
                List.copyOf(problemsById.keySet())
        );

        int correctProblemCount = 0;
        int directSolveCount = 0;
        int totalAttemptCount = 0;
        double totalLatestTestPassRate = 0.0;

        for (Long problemId : problemsById.keySet()) {
            List<LectureProblemSubmission> submissions =
                    submissionsByProblemId.getOrDefault(problemId, List.of());
            totalAttemptCount += submissions.size();

            boolean correct = submissions.stream().anyMatch(LectureProblemSubmission::correct);
            if (correct) {
                correctProblemCount++;
                if (!viewedProblemIds.contains(problemId)) {
                    directSolveCount++;
                }
            }

            LectureProblemSubmission latest = submissions.stream()
                    .max(this::compareSubmissionTime)
                    .orElse(null);
            totalLatestTestPassRate += calculateTestPassRate(latest);
        }

        return new LearningProfile(
                totalProblemCount,
                correctProblemCount,
                percentage(directSolveCount, totalProblemCount),
                viewedProblemIds.size(),
                percentage(viewedProblemIds.size(), totalProblemCount),
                roundFour(totalAttemptCount / (double) totalProblemCount),
                roundFour(totalLatestTestPassRate / totalProblemCount)
        );
    }

    private LearningContext buildLearningContext(Long courseId) {
        LearningCourse course = learningCoursePort.findActiveCourse(courseId);
        List<LectureContext> lectures = learningLecturePort.findLecturesByCourse(courseId)
                .stream()
                .limit(MAX_LECTURE_CONTEXT_COUNT)
                .map(this::toLectureContext)
                .toList();

        return new LearningContext(
                normalizeTitle(course.title(), "강좌 " + courseId),
                normalizeDescription(course.description()),
                lectures
        );
    }

    private LectureContext toLectureContext(LearningLecture lecture) {
        return new LectureContext(
                normalizeTitle(lecture.title(), "강의 " + lecture.lectureId()),
                normalizeDescription(lecture.description())
        );
    }

    private int compareSubmissionTime(
            LectureProblemSubmission left,
            LectureProblemSubmission right
    ) {
        Comparator<LocalDateTime> timeComparator = Comparator.nullsFirst(Comparator.naturalOrder());
        int timeCompared = timeComparator.compare(left.submittedAt(), right.submittedAt());
        if (timeCompared != 0) {
            return timeCompared;
        }
        return Comparator.nullsFirst(Long::compareTo).compare(
                left.lectureProblemSubmissionId(),
                right.lectureProblemSubmissionId()
        );
    }

    private double calculateTestPassRate(LectureProblemSubmission submission) {
        if (submission == null
                || submission.totalTestCount() == null
                || submission.totalTestCount() <= 0
                || submission.passedTestCount() == null) {
            return 0.0;
        }
        int passed = Math.max(0, Math.min(submission.passedTestCount(), submission.totalTestCount()));
        return passed * 100.0 / submission.totalTestCount();
    }

    private double percentage(int numerator, int denominator) {
        return roundFour(numerator * 100.0 / denominator);
    }

    private double roundFour(double value) {
        return Math.round(value * 10_000.0) / 10_000.0;
    }

    private String normalizeTitle(String value, String fallback) {
        String normalized = value == null || value.isBlank() ? fallback : value.strip();
        return normalized.substring(0, Math.min(normalized.length(), MAX_TITLE_LENGTH));
    }

    private String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        return normalized.substring(0, Math.min(normalized.length(), MAX_DESCRIPTION_LENGTH));
    }
}
