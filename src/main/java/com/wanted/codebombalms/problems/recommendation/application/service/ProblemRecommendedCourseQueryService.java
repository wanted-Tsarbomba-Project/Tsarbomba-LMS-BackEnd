package com.wanted.codebombalms.problems.recommendation.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationProblemPort;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadSavedRecommendedCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.query.GetProblemRecommendedCoursesQuery;
import com.wanted.codebombalms.problems.recommendation.application.query.GetRecommendedCourseEditViewQuery;
import com.wanted.codebombalms.problems.recommendation.application.query.GetSelectableRecommendedCoursesQuery;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetProblemRecommendedCoursesUseCase;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetRecommendedCourseEditViewUseCase;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetSelectableRecommendedCoursesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemRecommendedCourseQueryService implements GetRecommendedCourseEditViewUseCase,
        GetProblemRecommendedCoursesUseCase, GetSelectableRecommendedCoursesUseCase {

    private static final int DEFAULT_SELECTABLE_COURSE_LIMIT = 20;
    private static final int MAX_SELECTABLE_COURSE_LIMIT = 100;

    private final LoadRecommendationProblemPort loadRecommendationProblemPort;
    private final LoadSavedRecommendedCoursePort loadSavedRecommendedCoursePort;
    private final LoadRecommendationCoursePort loadRecommendationCoursePort;

    @Override
    public RecommendedCourseEditView handle(GetRecommendedCourseEditViewQuery query) {
        validateProblem(query.problemId());

        var savedCourses = loadSavedRecommendedCoursePort.loadSavedRecommendedCourses(query.problemId());
        var selectableCourses = loadRecommendationCoursePort.loadSelectableCourses(
                null,
                MAX_SELECTABLE_COURSE_LIMIT
        );
        selectableCourses = mergeSelectedCoursesIfMissing(savedCourses, selectableCourses);

        Map<Long, LoadSavedRecommendedCoursePort.SavedRecommendedCourseData> savedCourseMap =
                savedCourses.stream()
                        .collect(Collectors.toMap(
                                LoadSavedRecommendedCoursePort.SavedRecommendedCourseData::courseId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        var selectedCourseIds = savedCourses.stream()
                .map(LoadSavedRecommendedCoursePort.SavedRecommendedCourseData::courseId)
                .toList();

        var courseViews = selectableCourses.stream()
                .map(course -> {
                    var savedCourse = savedCourseMap.get(course.courseId());

                    return new SelectableCourseView(
                            course.courseId(),
                            course.title(),
                            course.description(),
                            course.thumbnailUrl(),
                            savedCourse != null,
                            savedCourse != null ? savedCourse.displayOrder() : null
                    );
                })
                .toList();

        return new RecommendedCourseEditView(
                query.problemId(),
                selectedCourseIds,
                courseViews
        );
    }

    @Override
    public List<RecommendedCourseView> handle(GetProblemRecommendedCoursesQuery query) {
        validateProblem(query.problemId());

        var savedCourses = loadSavedRecommendedCoursePort.loadSavedRecommendedCourses(query.problemId());
        var savedCourseIds = toCourseIds(savedCourses);
        var selectableCourses = loadRecommendationCoursePort.loadActiveCoursesByIds(savedCourseIds);

        var selectableCourseMap = selectableCourses.stream()
                .collect(Collectors.toMap(
                        LoadRecommendationCoursePort.SelectableCourseData::courseId,
                        Function.identity()
                ));

        return savedCourses.stream()
                .map(savedCourse -> {
                    var course = selectableCourseMap.get(savedCourse.courseId());

                    if (course == null) {
                        return null;
                    }

                    return new RecommendedCourseView(
                            course.courseId(),
                            course.title(),
                            course.description(),
                            course.thumbnailUrl(),
                            savedCourse.displayOrder()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<SelectableRecommendedCourseView> handle(GetSelectableRecommendedCoursesQuery query) {
        int limit = resolveLimit(query.limit());

        return loadRecommendationCoursePort.loadSelectableCourses(
                        query.keyword(),
                        limit
                )
                .stream()
                .map(course -> new SelectableRecommendedCourseView(
                        course.courseId(),
                        course.title(),
                        course.description(),
                        course.thumbnailUrl()
                ))
                .toList();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SELECTABLE_COURSE_LIMIT;
        }

        if (limit <= 0) {
            return DEFAULT_SELECTABLE_COURSE_LIMIT;
        }

        return Math.min(limit, MAX_SELECTABLE_COURSE_LIMIT);
    }

    private List<LoadRecommendationCoursePort.SelectableCourseData> mergeSelectedCoursesIfMissing(
            List<LoadSavedRecommendedCoursePort.SavedRecommendedCourseData> savedCourses,
            List<LoadRecommendationCoursePort.SelectableCourseData> selectableCourses
    ) {
        Set<Long> selectableCourseIds = selectableCourses.stream()
                .map(LoadRecommendationCoursePort.SelectableCourseData::courseId)
                .collect(Collectors.toSet());

        Set<Long> missingSelectedCourseIds = toCourseIds(savedCourses);
        missingSelectedCourseIds.removeAll(selectableCourseIds);

        if (missingSelectedCourseIds.isEmpty()) {
            return selectableCourses;
        }

        var missingCourses = loadRecommendationCoursePort.loadActiveCoursesByIds(missingSelectedCourseIds);

        return java.util.stream.Stream.concat(
                        selectableCourses.stream(),
                        missingCourses.stream()
                )
                .toList();
    }

    private Set<Long> toCourseIds(List<LoadSavedRecommendedCoursePort.SavedRecommendedCourseData> savedCourses) {
        return savedCourses.stream()
                .map(LoadSavedRecommendedCoursePort.SavedRecommendedCourseData::courseId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void validateProblem(Long problemId) {
        if (!loadRecommendationProblemPort.existsActiveProblem(problemId)) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
    }
}
