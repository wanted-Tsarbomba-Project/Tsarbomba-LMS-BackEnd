package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationResult;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.RankedProblemSet;
import com.wanted.codebombalms.learning.application.usecase.FinalProblemSetRankingUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.lecture.application.port.FinalProblemSetCandidatePort;
import com.wanted.codebombalms.lecture.application.port.LectureProgressPort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.application.usecase.FinalProblemSetRecommendationUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinalProblemSetRecommendationService implements FinalProblemSetRecommendationUseCase {

    private final LectureRepository lectureRepository;
    private final LectureProblemSetRepository lectureProblemSetRepository;
    private final FinalProblemSetCandidatePort finalProblemSetCandidatePort;
    private final LectureAccessPolicy lectureAccessPolicy;
    private final LectureProgressPort lectureProgressPort;
    private final FinalProblemSetRankingUseCase finalProblemSetRankingUseCase;

    @Override
    public List<FinalProblemSetCandidateView> findFinalProblemSetCandidates(Long lectureId, Long userId, boolean operator) {
        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));
        lectureAccessPolicy.validateLearningContentAccess(lecture, userId, operator);
        validateFinalProblemSetAvailable(lecture, userId, operator);

        if (lecture.getProblemCategoryId() == null) {
            return List.of();
        }

        Set<Long> mainProblemSetIds = lectureProblemSetRepository
                .findByCourseIdAndRole(lecture.getCourse().getCourseId(), LectureProblemSetRole.MAIN)
                .stream()
                .map(problemSet -> problemSet.getProblemSetId())
                .collect(Collectors.toSet());

        try {
            LearningRecommendationResult ranking = finalProblemSetRankingUseCase.rankFinalProblemSets(
                    userId,
                    lecture.getCourse().getCourseId(),
                    lectureId,
                    lecture.getProblemCategoryId(),
                    mainProblemSetIds,
                    operator
            );
            return toRankedViews(ranking, mainProblemSetIds);
        } catch (ExternalServiceException exception) {
            log.warn(
                    "event=final_problem_set_ai_fallback lectureId={} exceptionType={}",
                    lectureId,
                    exception.getClass().getSimpleName()
            );
            return findLegacyCandidates(lecture, mainProblemSetIds);
        }
    }

    private void validateFinalProblemSetAvailable(Lecture lecture, Long userId, boolean operator) {
        if (operator) {
            return;
        }
        if (lecture.getLectureOrder() == null || lectureRepository.existsNextLecture(
                lecture.getCourse().getCourseId(),
                lecture.getLectureOrder()
        )) {
            throw new ForbiddenException(LectureErrorCode.FINAL_PROBLEM_SET_NOT_AVAILABLE);
        }

        List<Long> requiredLectureIds = new ArrayList<>(
                lectureRepository.findPreviousLectureIds(
                        lecture.getCourse().getCourseId(),
                        lecture.getLectureOrder()
                )
        );
        requiredLectureIds.add(lecture.getLectureId());

        if (!lectureProgressPort.areLecturesCompleted(userId, requiredLectureIds)) {
            throw new ForbiddenException(LectureErrorCode.FINAL_PROBLEM_SET_NOT_AVAILABLE);
        }
    }

    private List<FinalProblemSetCandidateView> toRankedViews(
            LearningRecommendationResult ranking,
            Set<Long> excludedProblemSetIds
    ) {
        if (ranking == null || ranking.recommendations() == null) {
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_INVALID_RESPONSE);
        }

        List<Long> rankedIds = ranking.recommendations()
                .stream()
                .map(RankedProblemSet::problemSetId)
                .toList();
        if (rankedIds.stream().anyMatch(excludedProblemSetIds::contains)) {
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_INVALID_RESPONSE);
        }

        List<ProblemSetSummary> problemSets = finalProblemSetCandidatePort.findByProblemSetIds(rankedIds);
        if (problemSets.size() != rankedIds.size()) {
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_INVALID_RESPONSE);
        }

        Map<Long, ProblemSetSummary> problemSetById = new HashMap<>();
        for (ProblemSetSummary problemSet : problemSets) {
            problemSetById.put(problemSet.getProblemSetId(), problemSet);
        }

        return ranking.recommendations().stream()
                .map(item -> toView(problemSetById.get(item.problemSetId()), item))
                .toList();
    }

    private List<FinalProblemSetCandidateView> findLegacyCandidates(
            Lecture lecture,
            Set<Long> excludedProblemSetIds
    ) {
        return finalProblemSetCandidatePort.findCandidates(
                        lecture.getProblemCategoryId(),
                        excludedProblemSetIds,
                        LearningRecommendationClient.MAX_RECOMMENDATION_COUNT
                )
                .stream()
                .map(this::toFallbackView)
                .toList();
    }

    private FinalProblemSetCandidateView toView(
            ProblemSetSummary problemSet,
            RankedProblemSet ranked
    ) {
        return new FinalProblemSetCandidateView(
                problemSet.getProblemSetId(),
                problemSet.getProblemNumber(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getAccuracyRate(),
                problemSet.getCreatedAt(),
                ranked.score(),
                ranked.reasonCode().name(),
                ranked.recommendationReason()
        );
    }

    private FinalProblemSetCandidateView toFallbackView(ProblemSetSummary problemSet) {
        return new FinalProblemSetCandidateView(
                problemSet.getProblemSetId(),
                problemSet.getProblemNumber(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getAccuracyRate(),
                problemSet.getCreatedAt(),
                null,
                "COURSE_RELATED",
                "강좌에서 학습한 내용과 연관된 문제 세트예요."
        );
    }
}
