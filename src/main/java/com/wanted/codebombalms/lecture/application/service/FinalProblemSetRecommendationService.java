package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinalProblemSetRecommendationService implements FinalProblemSetRecommendationUseCase {

    private static final int MAX_RECOMMENDATION_COUNT = 2;

    private final LectureRepository lectureRepository;
    private final LectureProblemSetRepository lectureProblemSetRepository;
    private final FinalProblemSetCandidatePort finalProblemSetCandidatePort;
    private final LectureAccessPolicy lectureAccessPolicy;
    private final LectureProgressPort lectureProgressPort;

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

        return finalProblemSetCandidatePort.findCandidates(
                        lecture.getProblemCategoryId(),
                        mainProblemSetIds,
                        MAX_RECOMMENDATION_COUNT
                )
                .stream()
                .map(this::toView)
                .toList();
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

    private FinalProblemSetCandidateView toView(ProblemSetSummary problemSet) {
        return new FinalProblemSetCandidateView(
                problemSet.getProblemSetId(),
                problemSet.getProblemNumber(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getAccuracyRate(),
                problemSet.getCreatedAt()
        );
    }
}
