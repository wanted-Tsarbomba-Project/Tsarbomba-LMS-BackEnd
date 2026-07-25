package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationResult;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.RankedProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.ReasonCode;
import com.wanted.codebombalms.learning.application.usecase.FinalProblemSetRankingUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.lecture.application.port.FinalProblemSetCandidatePort;
import com.wanted.codebombalms.lecture.application.port.LectureProgressPort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FinalProblemSetRecommendationServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureProblemSetRepository lectureProblemSetRepository;

    @Mock
    private FinalProblemSetCandidatePort finalProblemSetCandidatePort;

    @Mock
    private LectureAccessPolicy lectureAccessPolicy;

    @Mock
    private LectureProgressPort lectureProgressPort;

    @Mock
    private FinalProblemSetRankingUseCase finalProblemSetRankingUseCase;

    @InjectMocks
    private FinalProblemSetRecommendationService service;

    @BeforeEach
    void setUpAiFallback() {
        lenient().when(finalProblemSetRankingUseCase.rankFinalProblemSets(
                        any(), anyLong(), anyLong(), anyLong(), anySet(), any(Boolean.class)
                ))
                .thenThrow(new ExternalServiceException(
                        LearningErrorCode.LEARNING_RECOMMENDATION_UNAVAILABLE
                ));
    }

    @Test
    void findFinalProblemSetCandidates_returnsEmpty_whenLectureHasNoProblemCategory() {
        Lecture lecture = lecture(10L, 1L, null);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 1)).willReturn(false);
        given(lectureProgressPort.areLecturesCompleted(20L, List.of(10L))).willReturn(true);

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        assertEquals(0, result.size());
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);
        verify(finalProblemSetCandidatePort, never()).findCandidates(any(), anySet(), anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFinalProblemSetCandidates_excludesCourseMainProblemSets() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 1)).willReturn(false);
        given(lectureProgressPort.areLecturesCompleted(20L, List.of(10L))).willReturn(true);
        given(lectureProblemSetRepository.findByCourseIdAndRole(1L, LectureProblemSetRole.MAIN))
                .willReturn(List.of(
                        LectureProblemSet.restore(100L, 1L, 10L, 4001L, LectureProblemSetRole.MAIN, 1),
                        LectureProblemSet.restore(101L, 1L, 11L, 4002L, LectureProblemSetRole.MAIN, 2)
                ));
        given(finalProblemSetCandidatePort.findCandidates(eq(3001L), org.mockito.ArgumentMatchers.anySet(), eq(2)))
                .willReturn(List.of(problemSet(4003L)));

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        ArgumentCaptor<Set<Long>> excludedCaptor = ArgumentCaptor.forClass(Set.class);
        verify(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);
        verify(finalProblemSetCandidatePort).findCandidates(eq(3001L), excludedCaptor.capture(), eq(2));
        assertEquals(Set.of(4001L, 4002L), excludedCaptor.getValue());
        assertEquals(1, result.size());
        assertEquals(4003L, result.get(0).problemSetId());
    }

    @Test
    void findFinalProblemSetCandidates_preservesPythonRankingAndReason() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 1)).willReturn(false);
        given(lectureProgressPort.areLecturesCompleted(20L, List.of(10L))).willReturn(true);
        given(lectureProblemSetRepository.findByCourseIdAndRole(1L, LectureProblemSetRole.MAIN))
                .willReturn(List.of());
        doReturn(new LearningRecommendationResult(
                "GEMINI_EMBEDDING_PERSONALIZED",
                List.of(
                        new RankedProblemSet(
                                4004L,
                                new BigDecimal("0.9123"),
                                ReasonCode.LEVEL_MATCHED,
                                "현재 학습 수준에 적합한 문제 세트예요."
                        ),
                        new RankedProblemSet(
                                4003L,
                                new BigDecimal("0.8123"),
                                ReasonCode.COURSE_RELATED,
                                "강좌 내용과 연관된 문제 세트예요."
                        )
                )
        )).when(finalProblemSetRankingUseCase).rankFinalProblemSets(
                20L, 1L, 10L, 3001L, Set.of(), false
        );
        given(finalProblemSetCandidatePort.findByProblemSetIds(List.of(4004L, 4003L)))
                .willReturn(List.of(problemSet(4004L), problemSet(4003L)));

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        assertEquals(List.of(4004L, 4003L), result.stream()
                .map(view -> view.problemSetId())
                .toList());
        assertEquals(new BigDecimal("0.9123"), result.get(0).score());
        assertEquals("LEVEL_MATCHED", result.get(0).reasonCode());
        assertEquals("현재 학습 수준에 적합한 문제 세트예요.", result.get(0).recommendationReason());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    @Test
    void findFinalProblemSetCandidates_checksPreviousAndCurrentLecturesCompleted() {
        Lecture lecture = lecture(10L, 1L, 3001L, 3);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 3)).willReturn(false);
        given(lectureRepository.findPreviousLectureIds(1L, 3)).willReturn(List.of(8L, 9L));
        given(lectureProgressPort.areLecturesCompleted(20L, List.of(8L, 9L, 10L))).willReturn(true);
        given(lectureProblemSetRepository.findByCourseIdAndRole(1L, LectureProblemSetRole.MAIN))
                .willReturn(List.of());
        given(finalProblemSetCandidatePort.findCandidates(eq(3001L), anySet(), eq(2)))
                .willReturn(List.of(problemSet(4003L)));

        var result = service.findFinalProblemSetCandidates(10L, 20L, false);

        assertEquals(1, result.size());
        verify(lectureProgressPort).areLecturesCompleted(20L, List.of(8L, 9L, 10L));
    }

    @Test
    void findFinalProblemSetCandidates_throwsForbidden_whenLectureIsNotLastLecture() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 1)).willReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        assertEquals(LectureErrorCode.FINAL_PROBLEM_SET_NOT_AVAILABLE, exception.getErrorCode());
        verify(lectureProgressPort, never()).areLecturesCompleted(anyLong(), any());
        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    @Test
    void findFinalProblemSetCandidates_throwsForbidden_whenLastLectureIsNotCompleted() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureRepository.existsNextLecture(1L, 1)).willReturn(false);
        given(lectureProgressPort.areLecturesCompleted(20L, List.of(10L))).willReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        assertEquals(LectureErrorCode.FINAL_PROBLEM_SET_NOT_AVAILABLE, exception.getErrorCode());
        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    @Test
    void findFinalProblemSetCandidates_allowsOperatorWithoutCompletionCheck() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        given(lectureProblemSetRepository.findByCourseIdAndRole(1L, LectureProblemSetRole.MAIN))
                .willReturn(List.of());
        given(finalProblemSetCandidatePort.findCandidates(eq(3001L), anySet(), eq(2)))
                .willReturn(List.of(problemSet(4003L)));

        var result = service.findFinalProblemSetCandidates(10L, null, true);

        assertEquals(1, result.size());
        verify(lectureRepository, never()).existsNextLecture(anyLong(), any());
        verify(lectureProgressPort, never()).areLecturesCompleted(anyLong(), any());
    }

    @Test
    void findFinalProblemSetCandidates_throwsNotFound_whenLectureDoesNotExist() {
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    @Test
    void findFinalProblemSetCandidates_propagatesForbidden_whenAccessDenied() {
        Lecture lecture = lecture(10L, 1L, 3001L);
        given(lectureRepository.findByLectureIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(lecture));
        doThrow(new ForbiddenException(LectureErrorCode.LECTURE_ACCESS_DENIED))
                .when(lectureAccessPolicy).validateLearningContentAccess(lecture, 20L, false);

        assertThrows(ForbiddenException.class, () ->
                service.findFinalProblemSetCandidates(10L, 20L, false)
        );

        verify(lectureProblemSetRepository, never()).findByCourseIdAndRole(anyLong(), any());
        verify(finalProblemSetCandidatePort, never()).findCandidates(anyLong(), anySet(), anyInt());
    }

    private Lecture lecture(Long lectureId, Long courseId, Long problemCategoryId) {
        return lecture(lectureId, courseId, problemCategoryId, 1);
    }

    private Lecture lecture(Long lectureId, Long courseId, Long problemCategoryId, Integer lectureOrder) {
        Course course = new Course();
        course.setCourseId(courseId);

        return Lecture.restore(
                lectureId,
                course,
                "Lecture",
                "description",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                "lecture.png",
                problemCategoryId,
                LectureStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                lectureOrder
        );
    }

    private ProblemSetSummary problemSet(Long problemSetId) {
        return ProblemSetSummary.of(
                problemSetId,
                1,
                "Problem Set",
                "description",
                "EASY",
                1,
                2,
                null,
                LocalDateTime.now()
        );
    }
}
