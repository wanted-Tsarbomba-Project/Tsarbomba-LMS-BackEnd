package com.wanted.codebombalms.problems.recommendation.application.service;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.recommendation.application.command.SaveProblemRecommendedCoursesCommand;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationProblemPort;
import com.wanted.codebombalms.problems.recommendation.application.port.SaveProblemRecommendedCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.port.SaveProblemRecommendedCoursePort.RecommendedCourseOrder;
import com.wanted.codebombalms.problems.recommendation.application.usecase.SaveProblemRecommendedCoursesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class ProblemRecommendedCourseCommandService implements SaveProblemRecommendedCoursesUseCase {

    private final LoadRecommendationProblemPort loadRecommendationProblemPort;
    private final LoadRecommendationCoursePort loadRecommendationCoursePort;
    private final SaveProblemRecommendedCoursePort saveProblemRecommendedCoursePort;

    @Override
    public SaveProblemRecommendedCoursesResult handle(SaveProblemRecommendedCoursesCommand command) {
        validateCommand(command);
        validateProblem(command.problemId());
        validateCourses(command.courseIds());

        List<RecommendedCourseOrder> courses = toRecommendedCourseOrders(command.courseIds());

        saveProblemRecommendedCoursePort.replaceRecommendedCourses(
                command.problemId(),
                courses
        );

        return new SaveProblemRecommendedCoursesResult(
                command.problemId(),
                command.courseIds().size(),
                command.courseIds()
        );
    }

    private void validateCommand(SaveProblemRecommendedCoursesCommand command) {
        if (command == null || command.problemId() == null || command.courseIds() == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }

        if (command.problemId() <= 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }

        Set<Long> uniqueCourseIds = new HashSet<>(command.courseIds());

        if (uniqueCourseIds.size() != command.courseIds().size()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }

        if (command.courseIds().stream().anyMatch(courseId -> courseId == null || courseId <= 0)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }
    }

    private void validateProblem(Long problemId) {
        if (!loadRecommendationProblemPort.existsActiveProblem(problemId)) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
    }

    private void validateCourses(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return;
        }

        Set<Long> requestedCourseIds = new HashSet<>(courseIds);
        Set<Long> activeCourseIds = loadRecommendationCoursePort.loadActiveCourseIds(requestedCourseIds);

        if (activeCourseIds.size() != requestedCourseIds.size()) {
            throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);
        }
    }

    private List<RecommendedCourseOrder> toRecommendedCourseOrders(List<Long> courseIds) {
        return IntStream.range(0, courseIds.size())
                .mapToObj(index -> new RecommendedCourseOrder(
                        courseIds.get(index),
                        index + 1
                ))
                .toList();
    }
}
